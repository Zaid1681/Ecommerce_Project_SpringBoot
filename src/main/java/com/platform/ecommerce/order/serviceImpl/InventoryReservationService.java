package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.inventory.service.InventoryService;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.entity.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class InventoryReservationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryReservationService.class);
    private final InventoryService inventoryService;

    public InventoryReservationService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void reserveInventory(Order order) {
        log.info("Reserving inventory for orderId={} items={}", order.getId(), order.getItems().size());
        for (OrderItem item : order.getItems()) {
            inventoryService.reserveStock(item.getProductId(), item.getQuantity());
        }
        log.info("Inventory reserved for orderId={}", order.getId());
    }
}
