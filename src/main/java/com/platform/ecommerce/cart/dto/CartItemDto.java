package com.platform.ecommerce.cart.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private Long productId;
    private Integer quantity;
    private Double price;
}
