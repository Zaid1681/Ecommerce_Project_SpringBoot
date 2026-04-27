package com.platform.ecommerce.order.mapper;

import com.platform.ecommerce.order.dto.CheckoutResponseDto;
import com.platform.ecommerce.order.dto.OrderItemDto;
import com.platform.ecommerce.order.dto.OrderResponseDto;
import com.platform.ecommerce.order.entity.Order;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class OrderMapper {
    public OrderResponseDto toDto(Order order) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setOrderId(order.getId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus().name());

        List<OrderItemDto> items = order.getItems().stream().map(i -> {
            OrderItemDto d = new OrderItemDto();
            d.setProductId(i.getProductId());
            d.setQuantity(i.getQuantity());
            d.setPrice(i.getPrice());
            return d;
        }).toList();

        dto.setItems(items);
        return dto;
    }

    public CheckoutResponseDto toCheckOutDto(Order order, Long paymentId){
        CheckoutResponseDto dto = new CheckoutResponseDto();
        dto.setOrderId(order.getId());
        dto.setAmount(order.getTotalAmount());
        dto.setPaymentId(paymentId);
        return dto;
    }
}
