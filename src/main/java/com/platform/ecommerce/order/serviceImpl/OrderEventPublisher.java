package com.platform.ecommerce.order.serviceImpl;

import com.platform.ecommerce.kafka.KafkaTopics;
import com.platform.ecommerce.kafka.events.OrderConfirmedEvent;
import com.platform.ecommerce.kafka.producer.KafkaEventProducer;
import com.platform.ecommerce.order.entity.Order;
import com.platform.ecommerce.user.entity.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);
    private final KafkaEventProducer kafkaEventProducer;

    public OrderEventPublisher(KafkaEventProducer kafkaEventProducer) {
        this.kafkaEventProducer = kafkaEventProducer;
    }

    public void publishOrderConfirmed(Order order, Users user) {
        OrderConfirmedEvent event = new OrderConfirmedEvent(
                order.getId(),
                order.getUserId(),
                user.getEmail(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
        kafkaEventProducer.send(KafkaTopics.ORDER_CONFIRMED, event);
        log.info("Published order-confirmed event for orderId={}", order.getId());
    }
}
