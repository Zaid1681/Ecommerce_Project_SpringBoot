package com.platform.ecommerce.order.controller;

import com.platform.ecommerce.order.dto.OrderResponseDto;
import com.platform.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {
    @Autowired private OrderService orderService;

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @PostMapping("")
    public OrderResponseDto placeOrder() {
        log.info("HTTP POST /api/order - placeOrder called");

        return orderService.placeOrder();
    }

    // 2. Get all orders of user
    @GetMapping("/user/{userId}")
    public List<OrderResponseDto> getUserOrders(@PathVariable Long userId) {
        log.info("HTTP GET /api/order/user/{} - getUserOrders", userId);
        return orderService.getUserOrders(userId);
    }

    // 3. Get single order
    @GetMapping("/{orderId}")
    public OrderResponseDto getOrder(@PathVariable Long orderId) {
        log.info("HTTP GET /api/order/{} - getOrder", orderId);
        return orderService.getOrderById(orderId);
    }

    // 4. Cancel order
    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        log.info("HTTP PUT /api/order/cancel/{} - cancelOrder", orderId);
        String res = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(res);
    }

    // 5. Track order status
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        log.info("HTTP GET /api/order/status/{} - getOrderStatus", orderId);
        String res = orderService.getOrderStatus(orderId);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/delete/{orderId}")
    public ResponseEntity<?> removeOrderItem(
            @PathVariable Long orderId) {

        log.info("HTTP DELETE /api/order/delete/{} - deleteOrder", orderId);
        String res = orderService.deleteOrder(orderId);
        return ResponseEntity.ok(res);
    }
}
