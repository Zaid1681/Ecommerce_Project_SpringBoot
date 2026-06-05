package com.platform.ecommerce.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ecommerce.kafka.events.OrderConfirmedEvent;
import com.platform.ecommerce.kafka.events.PaymentFailedEvent;
import com.platform.ecommerce.kafka.events.PaymentInitiatedEvent;
import com.platform.ecommerce.kafka.events.PaymentSucceededEvent;
import com.platform.ecommerce.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import com.platform.ecommerce.kafka.KafkaTopics;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationConsumer(ObjectMapper objectMapper,
                                NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = KafkaTopics.ORDER_CONFIRMED, groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeOrderConfirmed(String message) {
        try {
            OrderConfirmedEvent event = objectMapper.readValue(message, OrderConfirmedEvent.class);
            log.info("Consumed order-confirmed event for orderId={}", event.getOrderId());
            notificationService.sendOrderConfirmedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process order-confirmed event", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_INITIATED, groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentInitiated(String message) {
        try {
            PaymentInitiatedEvent event = objectMapper.readValue(message, PaymentInitiatedEvent.class);
            log.info("Consumed payment-initiated event for orderId={}", event.getOrderId());
            notificationService.sendPaymentInitiatedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment-initiated event", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_SUCCEEDED, groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentSucceeded(String message) {
        try {
            PaymentSucceededEvent event = objectMapper.readValue(message, PaymentSucceededEvent.class);
            log.info("Consumed payment-succeeded event for orderId={}", event.getOrderId());
            notificationService.sendPaymentSucceededEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment-succeeded event", e);
        }
    }

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumePaymentFailed(String message) {
        try {
            PaymentFailedEvent event = objectMapper.readValue(message, PaymentFailedEvent.class);
            log.info("Consumed payment-failed event for orderId={}", event.getOrderId());
            notificationService.sendPaymentFailedEmail(event);
        } catch (Exception e) {
            log.error("Failed to process payment-failed event", e);
        }
    }
}
