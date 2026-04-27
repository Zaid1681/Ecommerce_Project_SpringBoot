package com.platform.ecommerce.cart.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponseDto {
    private Long userId;
    private Double totalPrice;
    private List<CartItemDto> items;
}
