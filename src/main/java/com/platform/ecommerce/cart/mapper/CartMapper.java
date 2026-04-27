package com.platform.ecommerce.cart.mapper;

import com.platform.ecommerce.cart.dto.CartItemDto;
import com.platform.ecommerce.cart.dto.CartResponseDto;
import com.platform.ecommerce.cart.entity.Cart;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CartMapper {

    public CartResponseDto toDto(Cart cart){
        CartResponseDto dto = new CartResponseDto();
        dto.setUserId(cart.getUserId());
        dto.setTotalPrice(cart.getTotalPrice());
        List<CartItemDto> items = cart.getItems().stream()
                .map( i->{
                    CartItemDto d = new CartItemDto();
                    d.setProductId(i.getProductId());
                    d.setQuantity(i.getQuantity());
                    d.setPrice(i.getPrice());
                    return d;
                }).toList();
        dto.setItems(items);
        return dto;
    }
}
