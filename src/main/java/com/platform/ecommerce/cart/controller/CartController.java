package com.platform.ecommerce.cart.controller;

import com.platform.ecommerce.cart.dto.AddToCartRequestDto;
import com.platform.ecommerce.cart.dto.CartResponseDto;
import com.platform.ecommerce.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    @Autowired
    CartService cartService;

    @PostMapping("")
    public CartResponseDto addToCart(
            @RequestBody AddToCartRequestDto dto) {

        return cartService.addToCart(dto);
    }
    @GetMapping("/{userId}")
    public CartResponseDto getCart(@PathVariable Long userId) {
        return cartService.getCart(userId);
    }

    @PutMapping("/{userId}/{productId}")
    public ResponseEntity<?> removeItem(@PathVariable Long userId, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }
    @PutMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.clearCart(userId));
    }
}
