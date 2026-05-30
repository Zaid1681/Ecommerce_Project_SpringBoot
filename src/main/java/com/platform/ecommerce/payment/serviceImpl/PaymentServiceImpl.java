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
import com.platform.ecommerce.payment.dto.InitiatePaymentResponse;
import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.entity.Payment;
import com.platform.ecommerce.payment.mapper.PaymentMapper;
import com.platform.ecommerce.payment.repository.PaymentRepository;
import com.platform.ecommerce.payment.service.PaymentService;
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

        InitiatePaymentResponse response = new InitiatePaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(orderId);
        response.setPublishableKey(stripePublishableKey); // correct
        response.setClientSecret(intent.getClientSecret()); // correct
        response.setAmount(order.getTotalAmount());
        response.setReturnUrl(frontendUrl + "/payment/return?orderId=" + orderId);
        return response;
    }

    @Override
    public void markPaymentSuccess(Long orderId) {
        log.info("Confirm payment endpoint called for orderId={}", orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                        .orElseThrow(()-> new ResourceNotFoundException("Payment not found"));

        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);
        log.info("Payment marked SUCCESS: paymentId={} orderId={}", payment.getId(), orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(()-> new ResourceNotFoundException("Order not found"));

        // PAYMENT_PENDING -> PAID
        stateMachine.transition(order, OrderStatus.PAID);
        orderRepository.save(order);
        log.info("Order state transitioned to PAID: orderId={}", orderId);

    }


    // ───────────────────────────────────────────────────────
    // Step 2: /payments/status?paymentIntentId=pi_xxx
    // Called from the return_url page to show current state
    // Retrieves live status from Stripe (not just DB)
    // ───────────────────────────────────────────────────────
    // ==========================================
    // MAIN WEBHOOK HANDLER
    // ==========================================

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

        log.info("Payment succeeded. orderId={} paymentIntentId={}", order.getId(), stripeObject.getId());
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
        log.warn("Payment failed. orderId={} reason={}", order.getId(),
                stripeObject.getLastPaymentError() != null ? stripeObject.getLastPaymentError().getMessage() : "unknown");

    }

    private void handlePaymentCanceled(PaymentIntent intent, String eventId) {
        // treat cancellation same as failure
        handlePaymentFailed(intent, eventId);
    }

    @Override
    public void markSuccess(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));

        // ★ Idempotency — webhook can fire multiple times ★
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // checking payment status
        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new RuntimeException("Failed to create payment");
        }

        // checking order status — payment should be in PAYMENT_PENDING
        if (!order.getStatus().equals(OrderStatus.PAYMENT_PENDING)) {
            throw new RuntimeException("Failed to create payment");
        }

        // PAYMENT_PENDING -> PAID
        stateMachine.transition(order, OrderStatus.PAID);
        payment.setStatus(PaymentStatus.SUCCESS);

        cartService.clearCart(order.getUserId());

        // saving changes
        paymentRepository.save(payment);
        orderRepository.save(order);
    }

    @Override
    public void markFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));
        payment.setStatus(PaymentStatus.FAILED);

        Order order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!payment.getStatus().equals(PaymentStatus.PENDING)) {
            throw new RuntimeException("Payment process failed");
        }

        // move order to CANCELLED and restore stock
        stateMachine.transition(order, OrderStatus.CANCELLED);

        for (OrderItem item : order.getItems()) {
            inventoryService.restoreStock(item.getProductId(), item.getQuantity());
        }
        paymentRepository.save(payment);
        orderRepository.save(order);

    }

    @Override
    public PaymentResDto getPaymentDetailsBId(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment Details not found"));

        return paymentMapper.toDto(payment);
    }

    }


