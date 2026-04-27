package com.platform.ecommerce.cart.service;

import com.platform.ecommerce.cart.dto.AddToCartRequestDto;
import com.platform.ecommerce.cart.dto.CartResponseDto;

public interface CartService {
    CartResponseDto addToCart(Long userId, AddToCartRequestDto dto);
    CartResponseDto getCart(Long userId);
    String removeItem(Long userId, Long productId);
    String clearCart(Long userId);

}
