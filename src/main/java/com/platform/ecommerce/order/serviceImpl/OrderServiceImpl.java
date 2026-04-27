package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.cart.entity.Cart;
import com.platform.ecommerce.cart.repository.CartRepository;
import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.common.enums.PaymentStatus;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.inventory.service.InventoryService;
import com.platform.ecommerce.order.dto.CheckoutResponseDto;
import com.platform.ecommerce.order.dto.OrderResponseDto;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.entity.OrderItem;
import com.platform.ecommerce.order.mapper.OrderMapper;
import com.platform.ecommerce.order.repository.OrderRepository;
import com.platform.ecommerce.order.service.OrderService;
import com.platform.ecommerce.payment.entity.Payment;
import com.platform.ecommerce.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepo;

    @Autowired
    OrderRepository orderRepo;

    @Autowired
    OrderMapper mapper;
    @Autowired
    InventoryService inventoryService;
    @Autowired
    PaymentRepository paymentRepository;

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
public CheckoutResponseDto placeOrder(Long userId) {
    Cart cart = cartRepo.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

    if (cart.getItems().isEmpty()) {
        throw new RuntimeException("Cart is empty");
    }
    // 1 Creat Order
    Order order  = new Order();
    order.setUserId(userId);
    order.setStatus(OrderStatus.CREATED);
    order.setCreatedAt(LocalDateTime.now());
    List<OrderItem> items = cart.getItems().stream()
            .map((ci) ->{
                OrderItem oi = new OrderItem();
                oi.setProductId(ci.getProductId());
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(ci.getPrice());
                oi.setOrder(order);
                return oi;
            }).toList();
    order.setItems(items);

    double totalPrice = items.stream()
            .mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    order.setTotalAmount(totalPrice);
    Order saved = orderRepo.save(order);

    // 2️⃣ Reduce Stock
    for (OrderItem item : items) {
        inventoryService.reduceStock(item.getProductId(), item.getQuantity());
    }

    Payment payment = new Payment();
    payment.setOrderId(saved.getId());
    payment.setAmount(totalPrice);
    payment.setStatus(PaymentStatus.PENDING);
    payment.setCreatedDate(LocalDateTime.now());
    Payment savedPayment = paymentRepository.save(payment);

    return mapper.toCheckOutDto(saved, savedPayment.getId());
}

    @Override
    public List<OrderResponseDto> getUserOrders(Long userId) {
        return orderRepo.findByUserId(userId)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return mapper.toDto(order);
    }

    @Override
    public String cancelOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {

            throw new RuntimeException("Cannot cancel this order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        return "Order Cancel Success";

    }

    @Override
    public String getOrderStatus(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getStatus().name();
    }

    @Override
    public String deleteOrder(Long orderId) {
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
        return "Order Deleted Success";
    }

}
