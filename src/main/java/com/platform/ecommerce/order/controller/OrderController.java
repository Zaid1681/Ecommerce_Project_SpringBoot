package com.platform.ecommerce.order.controller;

import com.platform.ecommerce.order.dto.CheckoutResponseDto;
import com.platform.ecommerce.order.dto.OrderResponseDto;
import com.platform.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    OrderService orderService;

    @PostMapping("/{userId}")
    public CheckoutResponseDto placeOrder(@PathVariable Long userId) {
        return orderService.placeOrder(userId);
    }
    // 2. Get all orders of user
    @GetMapping("/user/{userId}")
    public List<OrderResponseDto> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }

    // 3. Get single order
    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    // 4. Cancel order
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        String res = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(res);
    }

    // 5. Track order status
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        String res = orderService.getOrderStatus(orderId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<?> removeOrderItem(
            @PathVariable Long orderId) {

        String res = orderService.deleteOrder(orderId);
        return ResponseEntity.ok(res);
    }
}
