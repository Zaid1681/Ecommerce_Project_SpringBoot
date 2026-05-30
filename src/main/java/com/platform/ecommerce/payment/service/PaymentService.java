package com.platform.ecommerce.payment.service;

import com.platform.ecommerce.payment.dto.InitiatePaymentResponse;
import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;

public interface PaymentService {

    void markSuccess(Long paymentId);
    void markFailed(Long paymentId);
    PaymentResDto getPaymentDetailsBId(Long id);
    void processWebhook(String rawPayload , String stripeSignatureHeader) throws EventDataObjectDeserializationException;

    InitiatePaymentResponse initiatePayment(Long orderId) throws StripeException;

    void markPaymentSuccess(Long orderId);
}
