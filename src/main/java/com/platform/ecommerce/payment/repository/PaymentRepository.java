package com.platform.ecommerce.payment.repository;

import com.platform.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {


   // used for idempotency — check if this webhook event was already processed
    boolean existsByLastWebhookEventId(String eventId);
    Optional<Payment> findByStripePaymentIntentId(String id);
    Optional<Payment> findByOrderId(Long id);
    Optional<Payment> findTopByStripePaymentIntentIdOrderByCreatedDateDesc(String intentId);

}