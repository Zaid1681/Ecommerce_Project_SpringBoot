package com.platform.ecommerce.notification.serviceImpl;

import com.platform.ecommerce.kafka.events.OrderConfirmedEvent;
import com.platform.ecommerce.kafka.events.PaymentFailedEvent;
import com.platform.ecommerce.kafka.events.PaymentInitiatedEvent;
import com.platform.ecommerce.kafka.events.PaymentSucceededEvent;
import com.platform.ecommerce.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final JavaMailSender mailSender;

    public NotificationServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOrderConfirmedEmail(OrderConfirmedEvent event) {
        log.info("Sending order confirmed email for orderId={}", event.getOrderId());
        String subject = "Your order is confirmed";
        String text = String.format(
                "Hi,\n\nYour order %d has been confirmed.\nTotal amount: %.2f\n\nThank you for shopping with us!\n",
                event.getOrderId(), event.getTotalAmount());
        sendEmail(event.getEmail(), subject, text);
    }

    @Override
    public void sendPaymentInitiatedEmail(PaymentInitiatedEvent event) {
        log.info("Sending payment initiated email for orderId={}", event.getOrderId());
        String subject = "Payment initiated for your order";
        String text = String.format(
                "Hi,\n\nWe have started payment for order %d.\nAmount: %.2f\n\nPlease complete checkout if required.\n",
                event.getOrderId(), event.getAmount());
        sendEmail(event.getEmail(), subject, text);
    }

    @Override
    public void sendPaymentSucceededEmail(PaymentSucceededEvent event) {
        log.info("Sending payment success email for orderId={}", event.getOrderId());
        String subject = "Payment successful";
        String text = String.format(
                "Hi,\n\nYour payment for order %d was successful.\nAmount: %.2f\n\nWe will start preparing your order now.\n",
                event.getOrderId(), event.getAmount());
        sendEmail(event.getEmail(), subject, text);
    }

    @Override
    public void sendPaymentFailedEmail(PaymentFailedEvent event) {
        log.info("Sending payment failed email for orderId={}", event.getOrderId());
        String subject = "Payment failed";
        String text = String.format(
                "Hi,\n\nYour payment for order %d failed.\nReason: %s\n\nPlease try again or contact support.\n",
                event.getOrderId(), event.getFailureReason());
        sendEmail(event.getEmail(), subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("zaidkhan1682@gmail.com");
            message.setSubject(subject);
            message.setText(text);
            message.setFrom("zaidkhan1781@gmail.com");
            mailSender.send(message);
            log.info("Email sent to {} with subject {}", "zaidkhan1682@gmail.com", subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", "zaidkhan1682@gmail.com", e.getMessage());
        }
    }
}
