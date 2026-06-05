package com.platform.ecommerce.shipping.controller;

import com.platform.ecommerce.shipping.dto.ShipmentDto;
import com.platform.ecommerce.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipping")
@CrossOrigin(origins = "${app.frontend.url}")
public class ShippingController {

    private static final Logger log = LoggerFactory.getLogger(ShippingController.class);

    @Autowired
    private ShippingService shippingService;

    /**
     * Start fulfillment for a paid order
     * Transitions order from PAID → PROCESSING
     */
    @PostMapping("/start-processing")
    public ResponseEntity<ShipmentDto> startProcessing(@RequestParam Long orderId) {
        log.info("HTTP POST /api/shipping/start-processing orderId={}", orderId);
        ShipmentDto shipment = shippingService.startProcessing(orderId);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Mark order as shipped
     * Transitions order from PROCESSING → SHIPPED
     * Requires tracking number and carrier information
     */
    @PostMapping("/mark-shipped")
    public ResponseEntity<ShipmentDto> markShipped(
            @RequestParam Long orderId,
            @RequestParam String trackingNumber,
            @RequestParam String carrier) {
        log.info("HTTP POST /api/shipping/mark-shipped orderId={} trackingNumber={} carrier={}",
                orderId, trackingNumber, carrier);
        ShipmentDto shipment = shippingService.markShipped(orderId, trackingNumber, carrier);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Mark order as delivered
     * Transitions order from SHIPPED → DELIVERED
     */
    @PostMapping("/mark-delivered")
    public ResponseEntity<ShipmentDto> markDelivered(@RequestParam Long orderId) {
        log.info("HTTP POST /api/shipping/mark-delivered orderId={}", orderId);
        ShipmentDto shipment = shippingService.markDelivered(orderId);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Get shipment details for an order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ShipmentDto> getShipment(@PathVariable Long orderId) {
        log.info("HTTP GET /api/shipping/{} - getShipment", orderId);
        ShipmentDto shipment = shippingService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(shipment);
    }
}
