package com.platform.ecommerce.inventory.service;

import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.product.entity.Product;
import com.platform.ecommerce.product.repository.ProductRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final int MAX_RESERVE_ATTEMPTS = 3;

    private final ProductRepository prodRepo;

    public InventoryService(ProductRepository prodRepo) {
        this.prodRepo = prodRepo;
    }

    @Transactional
    public void reserveStock(Long productId, int quantity) {
        for (int attempt = 1; attempt <= MAX_RESERVE_ATTEMPTS; attempt++) {
            try {
                log.info("Reserving stock for productId={} qty={} attempt={}", productId, quantity, attempt);
                Product product = prodRepo.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found " + productId));

                int stock = safeInt(product.getStock());
                int reserved = safeInt(product.getReservedQuantity());
                int available = stock - reserved;
                if (available < quantity) {
                    throw new RuntimeException(
                            "Insufficient stock for product " + productId +
                                    ". Available: " + available + ", requested: " + quantity
                    );
                }

                product.setReservedQuantity(reserved + quantity);
                prodRepo.save(product);
                prodRepo.flush();
                log.debug("Reserved updated for productId={}", productId);
                return;
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt == MAX_RESERVE_ATTEMPTS) {
                    log.error("Could not reserve stock after {} attempts for productId={}", MAX_RESERVE_ATTEMPTS, productId, e);
                    throw new RuntimeException("Failed to reserve stock due to concurrent updates", e);
                }
                log.warn("Optimistic lock conflict reserving stock for productId={} attempt={}/{}", productId, attempt, MAX_RESERVE_ATTEMPTS);
            }
        }
    }
    @Transactional
    public void confirmStock(Long productId, int quantity){
        log.info("Confirming stock for productId={} qty={}", productId, quantity);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found " + productId));

        product.setStock(safeInt(product.getStock()) - quantity);
        product.setReservedQuantity(safeInt(product.getReservedQuantity()) - quantity);
        prodRepo.save(product);
        log.debug("Stock confirmed for productId={}", productId);
    }

    @Transactional
    public void reduceStock(Long productId, int qty){
        log.info("Reducing stock for productId={} qty={}", productId, qty);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        int stock = safeInt(product.getStock());
        if(stock < qty){
            throw new RuntimeException("Insufficient Stock");
        }
        product.setStock(stock - qty);
        prodRepo.save(product);
        log.debug("Stock reduced for productId={}", productId);
    }

    @Transactional
    public void restoreStock(Long productId, int qty){
        log.info("Restoring stock for productId={} qty={}", productId, qty);
        Product product = prodRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setStock(safeInt(product.getStock()) + qty);
        prodRepo.save(product);
        log.debug("Stock restored for productId={}", productId);
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
