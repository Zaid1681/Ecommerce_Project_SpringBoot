package com.platform.ecommerce.cart.dto;

import lombok.Data;

@Data
public class AddToCartRequestDto {
    private Long productId;
    private Integer quantity;
}
