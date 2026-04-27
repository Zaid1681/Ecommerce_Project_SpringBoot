package com.platform.ecommerce.inventory.service;

import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    @Autowired
    ProductRepository prodRepo;

    @Transactional
    public void reduceStock(Long productId, int qty){
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if(product.getStock() < qty){
            throw new RuntimeException("Insufficient Stock");
        }
        product.setStock(product.getStock() - qty);

    }

    @Transactional
    public void restoreStock(Long productId, int qty){
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStock(product.getStock() + qty);
    }
}
