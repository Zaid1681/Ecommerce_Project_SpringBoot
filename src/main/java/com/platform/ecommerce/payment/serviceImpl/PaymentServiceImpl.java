package com.platform.ecommerce.payment.serviceImpl;

import com.platform.ecommerce.cart.service.CartService;
import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.common.enums.PaymentStatus;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.inventory.service.InventoryService;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.entity.OrderItem;
import com.platform.ecommerce.order.repository.OrderRepository;
import com.platform.ecommerce.order.statemachine.OrderStateMachine;
import com.platform.ecommerce.kafka.events.PaymentFailedEvent;
import com.platform.ecommerce.kafka.events.PaymentInitiatedEvent;
import com.platform.ecommerce.kafka.events.PaymentSucceededEvent;
import com.platform.ecommerce.kafka.producer.KafkaEventProducer;
import com.platform.ecommerce.payment.dto.InitiatePaymentResponse;
import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.entity.Payment;
import com.platform.ecommerce.payment.mapper.PaymentMapper;
import com.platform.ecommerce.payment.repository.PaymentRepository;
import com.platform.ecommerce.payment.service.PaymentService;
import com.platform.ecommerce.shipping.service.ShippingService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private CartService cartService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private InventoryService inventoryService;
    @Autowired private PaymentMapper paymentMapper;
    @Autowired private OrderStateMachine stateMachine;
    @Autowired private ShippingService shippingService;
    @Autowired private KafkaEventProducer kafkaEventProducer;
    @Autowired private com.platform.ecommerce.user.repository.UserRepository userRepository;
    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.frontend.url}")
    private String frontendUrl;


//    @Autowired
//    private StripeService stripeService;

    // ───────────────────────────────────────────────────────
    // Step 1: /initiate
    // Creates a Stripe PaymentIntent and saves stripePaymentIntentId to DB
    // Returns clientSecret to frontend
    // ───────────────────────────────────────────────────────
    @Override
    public InitiatePaymentResponse initiatePayment(Long orderId) throws StripeException {
        log.info("Initiating payment for orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        long amountInCents= Math.round(order.getTotalAmount()*100);
        log.debug("Order amount for orderId={} is {}", orderId, order.getTotalAmount());
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("inr")
                .putMetadata("orderId",String.valueOf(orderId))
                .putMetadata("userId", String.valueOf(order.getUserId()))
                // automatic_payment_methods lets Stripe show the best UI for the customer's region
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        PaymentIntent intent = PaymentIntent.create(params);

        Payment payment  = new Payment();
        payment.setOrderId(orderId);
        payment.setStripePaymentIntentId(intent.getId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(order.getTotalAmount());
        paymentRepository.save(payment);
        log.info("Payment record created: paymentId={} orderId={}", payment.getId(), orderId);

        // move order to PAYMENT_PENDING once a payment record is created
        stateMachine.transition(order, OrderStatus.PAYMENT_PENDING);
        orderRepository.save(order);
        log.info("Order moved to PAYMENT_PENDING: orderId={}", orderId);

        String email = userRepository.findById(order.getUserId())
                .map(user -> user.getEmail())
                .orElse("no-reply@ecommerce.com");

        PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                orderId,
                payment.getId(),
                order.getUserId(),
                email,
                order.getStatus().name(),
                order.getTotalAmount(),
                intent.getId(),
                order.getCreatedAt()
        );
        kafkaEventProducer.send(com.platform.ecommerce.kafka.KafkaTopics.PAYMENT_INITIATED, event);

        InitiatePaymentResponse response = new InitiatePaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(orderId);
        response.setPublishableKey(stripePublishableKey); // correct
        response.setClientSecret(intent.getClientSecret()); // correct
        response.setAmount(order.getTotalAmount());
        response.setReturnUrl(frontendUrl + "/payment/return?orderId=" + orderId);
        return response;
    }
    

    // ───────────────────────────────────────────────────────
    // MAIN WEBHOOK HANDLER
    // ───────────────────────────────────────────────────────

    public void processWebhook(String rawPayload, String stripeSignatureHeader) throws EventDataObjectDeserializationException {

        Event event;

        try {
            event = Webhook.constructEvent(
                    rawPayload,
                    stripeSignatureHeader,
                    webhookSecret
            );
        } catch (SignatureVerificationException e) {
            log.error("❌ Invalid Stripe signature: {}", e.getMessage());
            throw new RuntimeException("Invalid webhook signature");
        } catch (Exception e) {
            log.error("❌ Webhook parsing error", e);
            throw new RuntimeException("Webhook parsing error");
        }
        String eventType = event.getType();
        log.info("Webhook received: type={} eventId={}", event.getType(), event.getId());

        // ==========================================
        // IDEMPOTENCY CHECK
        // ==========================================
        if (paymentRepository.existsByLastWebhookEventId(event.getId())) {
            log.warn("⚠️ Duplicate event ignored: {}", event.getId());
            return;
        }
        log.warn("🔥 RECEIVED EVENT: {}", event.getType());
        // =========================================================
        // 🚫 Step 3: Ignore unwanted events EARLY
        // =========================================================
        if (!eventType.equals("payment_intent.succeeded") &&
                !eventType.equals("payment_intent.payment_failed") &&
                !eventType.equals("payment_intent.canceled")) {

            log.info("ℹ️ Ignoring event type: {}", eventType);
            return;
        }

        // ==========================================
// SAFE DESERIALIZATION
// ==========================================
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        PaymentIntent paymentIntent;

        if (deserializer.getObject().isPresent()) {
            paymentIntent = (PaymentIntent) deserializer.getObject().get();
        } else {
            log.warn("⚠️ Using unsafe deserialization for: {}", eventType);
            paymentIntent = (PaymentIntent) deserializer.deserializeUnsafe();
        }
        System.out.println("eventType"+ eventType);

        // ==========================================
        // EVENT ROUTING
        // ==========================================
        switch (eventType) {

            case "payment_intent.succeeded":
                handlePaymentSucceeded((PaymentIntent) paymentIntent, event.getId());
                break;

            case "payment_intent.payment_failed":
                handlePaymentFailed((PaymentIntent) paymentIntent, event.getId());
                break;

            case "payment_intent.canceled":
                handlePaymentCanceled((PaymentIntent) paymentIntent, event.getId());
                break;

            default:
                log.info("ℹ️ Unhandled event type: {}", event.getType());
        }
    }



    // ───────────────────────────────────────────────────────
    // markSuccess — ONLY ever called from webhook handler
    // ────────── ────────── ────────── ────────── ──────────
    private void handlePaymentSucceeded(PaymentIntent stripeObject, String id) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripeObject.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Payment not found" + stripeObject.getId()));
//        if(payment.getStatus()==PaymentStatus.SUCCESS){
//            log.info("Payment already marked successm skipping. paymentid = {}" ,payment.getId());
//            return;
//        }
        System.out.println("payment" + payment);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + payment.getOrderId()));

        // State machine transition: PAYMENT_PENDING → PAID
        stateMachine.transition(order, OrderStatus.PAID);

        for(OrderItem item: order.getItems()){
            inventoryService.confirmStock(item.getProductId(), item.getQuantity());
        }
        // payment state changed
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setLastWebhookEventId(id);
        cartService.clearCart(order.getUserId());
        paymentRepository.save(payment);
        orderRepository.save(order);

        PaymentSucceededEvent event = new PaymentSucceededEvent(
                order.getId(),
                payment.getId(),
                order.getUserId(),
                userRepository.findById(order.getUserId()).map(u -> u.getEmail()).orElse("no-reply@ecommerce.com"),
                order.getStatus().name(),
                order.getTotalAmount(),
                stripeObject.getId(),
                order.getCreatedAt()
        );
        kafkaEventProducer.send(com.platform.ecommerce.kafka.KafkaTopics.PAYMENT_SUCCEEDED, event);

        log.info("Payment succeeded. orderId={} paymentIntentId={}", order.getId(), stripeObject.getId());

        // Start fulfillment process: PAID → PROCESSING
        try {
            shippingService.startProcessing(order.getId());
            log.info("Fulfillment started for orderId={}", order.getId());
        } catch (Exception e) {
            log.error("Failed to start fulfillment for orderId={}: {}", order.getId(), e.getMessage());
            // Log the error but don't fail the webhook response
            // Fulfillment can be retried manually via shipping API
        }
    }

    // ───────────────────────────────────────────────────────
    // markFailed — ONLY ever called from webhook handler
    // ───────────────────────────────────────────────────────
    private void handlePaymentFailed(PaymentIntent stripeObject, String id) {
        Payment payment = paymentRepository.findByStripePaymentIntentId(stripeObject.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Payment not found" + stripeObject.getId()));
        if(payment.getStatus()==PaymentStatus.FAILED){
            log.info("Payment already marked failed, skipping. paymentId={}", payment.getId());
            return;
        }
        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found: " + payment.getOrderId()));

        // Transition to CANCELLED (from PAYMENT_PENDING) and restore stock
        stateMachine.transition(order, OrderStatus.CANCELLED);

        for(OrderItem item: order.getItems()){
            inventoryService.restoreStock(item.getProductId(), item.getQuantity());
        }

        // payment state is changed
        payment.setStatus(PaymentStatus.FAILED);
        payment.setLastWebhookEventId(id);

        paymentRepository.save(payment);
        orderRepository.save(order);

        PaymentFailedEvent event = new PaymentFailedEvent(
                order.getId(),
                payment.getId(),
                order.getUserId(),
                userRepository.findById(order.getUserId()).map(u -> u.getEmail()).orElse("no-reply@ecommerce.com"),
                order.getStatus().name(),
                order.getTotalAmount(),
                stripeObject.getId(),
                stripeObject.getLastPaymentError() != null ? stripeObject.getLastPaymentError().getMessage() : "unknown",
                order.getCreatedAt()
        );
        kafkaEventProducer.send(com.platform.ecommerce.kafka.KafkaTopics.PAYMENT_FAILED, event);

        log.warn("Payment failed. orderId={} reason={}", order.getId(),
                stripeObject.getLastPaymentError() != null ? stripeObject.getLastPaymentError().getMessage() : "unknown");

    }

    private void handlePaymentCanceled(PaymentIntent intent, String eventId) {
        // treat cancellation same as failure
        handlePaymentFailed(intent, eventId);
    }

    @Override
    public PaymentResDto getPaymentDetailsBId(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));

        return paymentMapper.toDto(payment);
    }

}


