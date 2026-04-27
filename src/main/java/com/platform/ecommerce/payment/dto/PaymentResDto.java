package com.platform.ecommerce.payment.dto;

import com.platform.ecommerce.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResDto {
    private Long paymentId;
    private Long orderId;
    private Double amount;
    private PaymentStatus status;
    private LocalDateTime createdDate;
    private String method;
    private String transactionId;
}
