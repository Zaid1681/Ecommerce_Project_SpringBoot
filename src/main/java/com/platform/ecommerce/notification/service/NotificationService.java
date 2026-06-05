package com.platform.ecommerce.notification.service;

import com.platform.ecommerce.kafka.events.OrderConfirmedEvent;
import com.platform.ecommerce.kafka.events.PaymentFailedEvent;
import com.platform.ecommerce.kafka.events.PaymentInitiatedEvent;
import com.platform.ecommerce.kafka.events.PaymentSucceededEvent;

public interface NotificationService {

    void sendOrderConfirmedEmail(OrderConfirmedEvent event);

    void sendPaymentInitiatedEmail(PaymentInitiatedEvent event);

    void sendPaymentSucceededEmail(PaymentSucceededEvent event);

    void sendPaymentFailedEmail(PaymentFailedEvent event);
}
