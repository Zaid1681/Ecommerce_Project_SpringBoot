package com.platform.ecommerce.shipping.serviceImpl;

import com.platform.ecommerce.common.enums.OrderStatus;
import com.platform.ecommerce.exceptons.ResourceNotFoundException;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.order.repository.OrderRepository;
import com.platform.ecommerce.order.statemachine.OrderStateMachine;
import com.platform.ecommerce.shipping.dto.ShipmentDto;
import com.platform.ecommerce.shipping.entity.Shipment;
import com.platform.ecommerce.shipping.repository.ShipmentRepository;
import com.platform.ecommerce.shipping.service.ShippingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ShippingServiceImpl implements ShippingService {

    private static final Logger log = LoggerFactory.getLogger(ShippingServiceImpl.class);

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStateMachine stateMachine;

    @Override
    public ShipmentDto startProcessing(Long orderId) {
        log.info("Starting processing for orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        // Verify order is in PAID state
        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException(
                    "Cannot start processing. Order must be in PAID state, current: " + order.getStatus()
            );
        }

        // Move order to PROCESSING
        stateMachine.transition(order, OrderStatus.PROCESSING);
        orderRepository.save(order);
        log.info("Order moved to PROCESSING: orderId={}", orderId);

        // Create shipment record
        Shipment shipment = new Shipment(orderId, "PROCESSING");
        shipment.setUpdatedAt(LocalDateTime.now());
        Shipment saved = shipmentRepository.save(shipment);
        log.info("Shipment record created: shipmentId={} orderId={}", saved.getId(), orderId);

        return toDto(saved);
    }

    @Override
    public ShipmentDto markShipped(Long orderId, String trackingNumber, String carrier) {
        log.info("Marking order SHIPPED: orderId={} trackingNumber={} carrier={}",
                orderId, trackingNumber, carrier);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for orderId: " + orderId));

        // Verify order is in PROCESSING state
        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException(
                    "Cannot mark shipped. Order must be in PROCESSING state, current: " + order.getStatus()
            );
        }

        // Move order to SHIPPED
        stateMachine.transition(order, OrderStatus.SHIPPED);
        orderRepository.save(order);
        log.info("Order moved to SHIPPED: orderId={}", orderId);

        // Update shipment
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        shipment.setStatus("SHIPPED");
        shipment.setShippedDate(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        Shipment updated = shipmentRepository.save(shipment);
        log.info("Shipment updated to SHIPPED: shipmentId={} trackingNumber={}", updated.getId(), trackingNumber);

        return toDto(updated);
    }

    @Override
    public ShipmentDto markDelivered(Long orderId) {
        log.info("Marking order DELIVERED: orderId={}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for orderId: " + orderId));

        // Verify order is in SHIPPED state
        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException(
                    "Cannot mark delivered. Order must be in SHIPPED state, current: " + order.getStatus()
            );
        }

        // Move order to DELIVERED
        stateMachine.transition(order, OrderStatus.DELIVERED);
        orderRepository.save(order);
        log.info("Order moved to DELIVERED: orderId={}", orderId);

        // Update shipment
        shipment.setStatus("DELIVERED");
        shipment.setDeliveredDate(LocalDateTime.now());
        shipment.setUpdatedAt(LocalDateTime.now());
        Shipment updated = shipmentRepository.save(shipment);
        log.info("Shipment marked DELIVERED: shipmentId={}", updated.getId());

        return toDto(updated);
    }

    @Override
    public ShipmentDto getShipmentByOrderId(Long orderId) {
        log.info("Fetching shipment for orderId={}", orderId);

        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for orderId: " + orderId));

        return toDto(shipment);
    }

    private ShipmentDto toDto(Shipment shipment) {
        return new ShipmentDto(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getTrackingNumber(),
                shipment.getCarrier(),
                shipment.getStatus(),
                shipment.getShippedDate(),
                shipment.getDeliveredDate(),
                shipment.getCreatedAt()
        );
    }
}
