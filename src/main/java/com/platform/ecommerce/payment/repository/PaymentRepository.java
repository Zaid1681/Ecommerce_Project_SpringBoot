package com.platform.ecommerce.payment.repository;

import com.platform.ecommerce.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}