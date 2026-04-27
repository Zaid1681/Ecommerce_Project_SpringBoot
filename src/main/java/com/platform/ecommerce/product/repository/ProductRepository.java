package com.platform.ecommerce.product.repository;

import com.platform.ecommerce.product.entity.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByProductName(String productName);
    List<Product> findByIsActiveTrue();

}
