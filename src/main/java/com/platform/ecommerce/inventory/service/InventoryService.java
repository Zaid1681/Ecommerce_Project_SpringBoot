package com.platform.ecommerce.inventory.service;

import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    ProductRepository prodRepo;


    @Transactional
    public void reserveStock(Long productId, int quantity){
        log.info("Reserving stock for productId={} qty={}", productId, quantity);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"+productId));

        int available = product.getStock()-product.getReservedQuantity();
        if(available < quantity){
            throw new RuntimeException(
                    "Insufficient stock for product " + productId +
                            ". Available: " + available + ", requested: " + quantity
            );
        }

        product.setReservedQuantity(product.getReservedQuantity()+quantity);
        prodRepo.save(product);
        log.debug("Reserved updated for productId={}", productId);

    }
    @Transactional
    public void confirmStock(Long productId, int quantity){
        log.info("Confirming stock for productId={} qty={}", productId, quantity);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"+productId));

        product.setStock(product.getStock() - quantity);
        product.setReservedQuantity(product.getReservedQuantity() - quantity);
        prodRepo.save(product);
        log.debug("Stock confirmed for productId={}", productId);
    }

    @Transactional
    public void reduceStock(Long productId, int qty){
        log.info("Reducing stock for productId={} qty={}", productId, qty);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if(product.getStock() < qty){
            throw new RuntimeException("Insufficient Stock");
        }
        product.setStock(product.getStock() - qty);
        log.debug("Stock reduced for productId={}", productId);

    }

    @Transactional
    public void restoreStock(Long productId, int qty){
        log.info("Restoring stock for productId={} qty={}", productId, qty);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStock(product.getStock() + qty);
        log.debug("Stock restored for productId={}", productId);
    }
}
