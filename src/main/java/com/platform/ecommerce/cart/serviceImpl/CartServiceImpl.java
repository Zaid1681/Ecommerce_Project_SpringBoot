package com.platform.ecommerce.cart.serviceImpl;

import com.platform.ecommerce.cart.dto.AddToCartRequestDto;
import com.platform.ecommerce.cart.dto.CartItemDto;
import com.platform.ecommerce.cart.dto.CartResponseDto;
import com.platform.ecommerce.cart.entity.Cart;
import com.platform.ecommerce.cart.entity.CartItem;
import com.platform.ecommerce.cart.mapper.CartMapper;
import com.platform.ecommerce.cart.repository.CartRepository;
import com.platform.ecommerce.cart.service.CartService;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartRepository cartRepo;

    @Autowired
    ProductRepository productRepo;

    @Autowired
    CartMapper mapper;

    @Override
    public CartResponseDto addToCart(Long userId, AddToCartRequestDto dto) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseGet(()->{
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return c;
                });
        Product product = productRepo.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if already exist the cartItems
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(dto.getProductId()))
                .findFirst();
        if(existing.isPresent()){
            existing.get().setQuantity(existing.get().getQuantity() + dto.getQuantity());
        }else{
            CartItem item = new CartItem();
            item.setProductId(dto.getProductId());
            item.setQuantity(dto.getQuantity());
            item.setPrice(product.getPrice());
            item.setCart(cart);
            cart.getItems().add(item);
        }
        double totalPrice = cart.getItems().stream()
                .mapToDouble(i -> i.getPrice()*i.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);
        Cart saved = cartRepo.save(cart);
        return mapper.toDto(saved);
    }

    @Override
    public CartResponseDto getCart(Long userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return mapper.toDto(cart);
    }

    @Override
    public String removeItem(Long userId, Long productId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        double total = cart.getItems().stream()
                        .mapToDouble(i -> i.getPrice()*i.getQuantity())
                                .sum();
        cart.setTotalPrice(total);
        cartRepo.save(cart);
        return "Item Removed Successfully";
    }

    @Override
    public String clearCart(Long userId) {
        Cart cart = cartRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);
        return "Cart Clear Successfully";
    }
}
