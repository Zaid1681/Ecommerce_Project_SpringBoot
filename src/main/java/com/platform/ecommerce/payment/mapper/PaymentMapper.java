package com.platform.ecommerce.payment.mapper;

import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentResDto toDto(Payment payment) {
        PaymentResDto dto = new PaymentResDto();
        dto.setPaymentId(payment.getId());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());
        dto.setMethod(payment.getMethod());
        dto.setCreatedDate(payment.getCreatedDate());
        dto.setTransactionId(payment.getTransactionId());
        dto.setOrderId(payment.getOrderId());

        return dto;
    }
}
