package com.platform.ecommerce.payment.controller;

import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @PostMapping("/{paymentId}/success")
    public ResponseEntity<?> success(@PathVariable Long paymentId) {
        paymentService.markSuccess(paymentId);
        return ResponseEntity.ok("Payment successful");
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<?> fail(@PathVariable Long paymentId) {
        paymentService.markFailed(paymentId);
        return ResponseEntity.ok("Payment failed");
    }
    @GetMapping("/{paymentId}")
    public PaymentResDto getPaymentById(@PathVariable Long paymentId) {
        return paymentService.getPaymentDetailsBId(paymentId);
    }
}
