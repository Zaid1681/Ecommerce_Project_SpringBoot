package com.platform.ecommerce.cart.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private Integer quantity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
