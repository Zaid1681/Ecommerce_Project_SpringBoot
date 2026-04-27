package com.platform.ecommerce.payment.entity;

import com.platform.ecommerce.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Getter
@Setter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdDate;

    private String method;
    private String transactionId;
}
