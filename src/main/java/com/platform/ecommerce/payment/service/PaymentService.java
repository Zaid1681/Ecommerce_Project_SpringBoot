package com.platform.ecommerce.payment.service;

import com.platform.ecommerce.payment.dto.PaymentResDto;

public interface PaymentService {

    void markSuccess(Long paymentId);
    void markFailed(Long paymentId);
    PaymentResDto getPaymentDetailsBId(Long id);

}
