package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.cart.entity.Cart;
import com.platform.ecommerce.cart.repository.CartRepository;
import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.order.dto.OrderResponseDto;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.mapper.OrderMapper;
import com.platform.ecommerce.order.repository.OrderRepository;
import com.platform.ecommerce.order.service.OrderService;
import com.platform.ecommerce.session.UserSession;
import com.platform.ecommerce.user.entity.Users;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final OrderMapper mapper;
    private final OrderCreationService orderCreationService;
    private final InventoryReservationService inventoryReservationService;
    private final OrderStateTransitionService orderStateTransitionService;
    private final OrderEventPublisher orderEventPublisher;
    private final UserSession userSession;

    public OrderServiceImpl(
            CartRepository cartRepo,
            OrderRepository orderRepo,
            OrderMapper mapper,
            OrderCreationService orderCreationService,
            InventoryReservationService inventoryReservationService,
            OrderStateTransitionService orderStateTransitionService,
            OrderEventPublisher orderEventPublisher,
            UserSession userSession
    ) {
        this.cartRepo = cartRepo;
        this.orderRepo = orderRepo;
        this.mapper = mapper;
        this.orderCreationService = orderCreationService;
        this.inventoryReservationService = inventoryReservationService;
        this.orderStateTransitionService = orderStateTransitionService;
        this.orderEventPublisher = orderEventPublisher;
        this.userSession = userSession;
    }
//    @Override
//    public CheckoutResponseDto placeOrder(Long userId) {
//        Cart cart = cartRepo.findByUserId(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
//
//        if (cart.getItems().isEmpty()) {
//            throw new RuntimeException("Cart is empty");
//        }
//        Order order  = new Order();
//        order.setUserId(userId);
//        order.setStatus(OrderStatus.CREATED);
//        order.setCreatedAt(LocalDateTime.now());
//        List<OrderItem> items = cart.getItems().stream()
//                .map((ci) ->{
//                    OrderItem oi = new OrderItem();
//                    oi.setProductId(ci.getProductId());
//                    oi.setQuantity(ci.getQuantity());
//                    oi.setPrice(ci.getPrice());
//                    oi.setOrder(order);
//                    return oi;
//                }).toList();
//        order.setItems(items);
//
//        double totalPrice = items.stream()
//                .mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
//        order.setTotalAmount(totalPrice);
//        Order saved = orderRepo.save(order);
//
//        // clear cart
//        cart.getItems().clear();
//        cart.setTotalPrice(0.0);
//        cartRepo.save(cart);
//        return mapper.toDto(saved);
//    }
    @Override
    public OrderResponseDto placeOrder() {
        Users user = userSession.getCurrentUser();
        Long userId = user.getId();
        log.info("Place order requested by userId={}", userId);

        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Order order = orderCreationService.buildOrder(userId, cart);

        log.info("Reserving stock for {} items for userId={}", order.getItems().size(), userId);
        inventoryReservationService.reserveInventory(order);

        log.info("Reserving complete for userId={} moving order to CONFIRMED", userId);
        orderStateTransitionService.confirmOrder(order);

        Order saved = orderRepo.save(order);
        log.info("Order created: orderId={} itemCount={}", saved.getId(), saved.getItems().size());
        log.debug("Order created details: userId={} total={}", userId, saved.getTotalAmount());

        orderEventPublisher.publishOrderConfirmed(saved, user);
        return mapper.toDto(saved);
//    Payment payment = new Payment();
//    payment.setOrderId(saved.getId());
//    payment.setAmount(totalPrice);
//    payment.setStatus(PaymentStatus.PENDING);
//    payment.setCreatedDate(LocalDateTime.now());
//    Payment savedPayment = paymentRepository.save(payment);
//
//    return mapper.toCheckOutDto(saved, savedPayment.getId());
}

    @Override
    public List<OrderResponseDto> getUserOrders(Long userId) {
        log.info("Fetching orders for userId={}", userId);
        List<OrderResponseDto> res = orderRepo.findByUserId(userId)
            .stream()
            .map(mapper::toDto)
            .toList();
        log.info("Found {} orders for userId={}", res.size(), userId);
        return res;
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        log.info("Fetching order by id={}", orderId);
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapper.toDto(order);
    }

    @Override
    public String cancelOrder(Long orderId) {
        log.info("Cancel request for orderId={}", orderId);
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {

            throw new RuntimeException("Cannot cancel this order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        log.info("Order cancelled orderId={}", orderId);
        return "Order Cancel Success";

    }

    @Override
    public String getOrderStatus(Long orderId) {
        log.info("Status request for orderId={}", orderId);
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getStatus().name();
    }

    @Override
    public String deleteOrder(Long orderId) {
        log.info("Delete request for orderId={}", orderId);
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // ❌ Restriction
        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot modify this order");
        }
        // remove item
        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException("Only CANCELLED orders can be deleted");
        }
        orderRepo.deleteById(orderId);
        log.info("Order deleted orderId={}", orderId);
        return "Order Deleted Success";
    }

}
