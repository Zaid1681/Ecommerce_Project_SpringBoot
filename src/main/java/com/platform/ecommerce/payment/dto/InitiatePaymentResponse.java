package com.platform.ecommerce.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitiatePaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String clientSecret;       // ★ "pi_xxx_secret_yyy" — passed to stripe.confirmPayment()
    private String publishableKey;     // your pk_test_xxx
    private Double amount;
    private String returnUrl;
}
