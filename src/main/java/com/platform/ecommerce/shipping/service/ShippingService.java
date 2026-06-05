package com.platform.ecommerce.shipping.service;

import com.platform.ecommerce.shipping.dto.ShipmentDto;

public interface ShippingService {
    
    /**
     * Moves order to PROCESSING state and creates fulfillment record
     * Called when payment is successful
     */
    ShipmentDto startProcessing(Long orderId);
    
    /**
     * Marks order as SHIPPED with tracking information
     */
    ShipmentDto markShipped(Long orderId, String trackingNumber, String carrier);
    
    /**
     * Marks order as DELIVERED
     */
    ShipmentDto markDelivered(Long orderId);
    
    /**
     * Get shipment details for an order
     */
    ShipmentDto getShipmentByOrderId(Long orderId);
}
