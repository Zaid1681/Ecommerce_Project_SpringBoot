package com.platform.ecommerce.order.service;

import com.platform.ecommerce.order.dto.CheckoutResponseDto;
import com.platform.ecommerce.order.dto.OrderResponseDto;

import java.util.List;

public interface OrderService {
    CheckoutResponseDto placeOrder(Long userId);
    List<OrderResponseDto> getUserOrders(Long userId);

    OrderResponseDto getOrderById(Long orderId);

    String cancelOrder(Long orderId);

    String getOrderStatus(Long orderId);
    String deleteOrder(Long orderId);
}
