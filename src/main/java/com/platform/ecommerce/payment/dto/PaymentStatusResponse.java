package com.platform.ecommerce.payment.dto;

import com.platform.ecommerce.common.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentStatusResponse {
    private String paymentIntentId;
    private String stripeStatus;       // "succeeded", "requires_payment_method", etc.
    private PaymentStatus ourStatus;   // maps to our PaymentStatus enum
    private Long orderId;
}
