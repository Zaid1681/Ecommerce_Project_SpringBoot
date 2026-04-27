package com.platform.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDto {

    private Long orderId;
    private Long paymentId;
    private Double amount;
}