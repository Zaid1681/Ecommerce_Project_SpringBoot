package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.cart.entity.Cart;
import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.entity.OrderItem;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderCreationService {

    public OrderCreationService() {
    }

    public Order buildOrder(Long userId, Cart cart) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> items = cart.getItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(cartItem.getProductId());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setOrder(order);
                    return orderItem;
                })
                .toList();

        order.setItems(items);
        order.setTotalAmount(items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum());

        return order;
    }
}
