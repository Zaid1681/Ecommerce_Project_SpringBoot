package com.platform.ecommerce.payment.controller;

import com.platform.ecommerce.payment.dto.InitiatePaymentResponse;
import com.platform.ecommerce.payment.dto.PaymentResDto;
import com.platform.ecommerce.payment.service.PaymentService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "${app.frontend.url}")

public class PaymentController {

    @Autowired  private PaymentService paymentService;
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @PostMapping(value = "/initiate")
    public InitiatePaymentResponse initiatePayment(@RequestParam Long orderId) throws StripeException {
        log.info("HTTP POST /api/payment/initiate orderId={}", orderId);
        return paymentService.initiatePayment(orderId);
    }

    // ★ Stripe webhook endpoint — CRITICAL: must receive RAW body bytes ★
    // Do NOT use @RequestBody String — Jackson will alter the body and break signature
    @PostMapping(value = "/webhook", consumes = "application/json")
    public ResponseEntity<String> webhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String stripeSignature) {
        log.info("HTTP POST /api/payment/webhook called");

        // Read raw bytes — signature verification requires exact original bytes
        String rawBody;
        try {
            rawBody = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
//            log.error("Failed to read webhook body: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Cannot read body");
        }

        try {
            paymentService.processWebhook(rawBody, stripeSignature);
            return ResponseEntity.ok("received");
        } catch (RuntimeException e) {
            System.err.println(e);
//            log.error("Webhook processing error: {}", e.getMessage());
//            if (e.getMessage().contains("Invalid Stripe webhook signature")) {
//                return ResponseEntity.status(400).body("Invalid signature");
//            }
            // Return 200 for business-logic errors — Stripe will retry on 5xx
            // but there's no point retrying a "payment not found" error
//            return ResponseEntity.ok("error noted");
//            log.error("Webhook error", e);
        } catch (EventDataObjectDeserializationException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("success");
    }



    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestParam Long orderId) {
        log.info("HTTP POST /api/payment/confirm orderId={}", orderId);
        paymentService.markPaymentSuccess(orderId);
        return ResponseEntity.ok("Payment confirmed");
    }

    @PostMapping("/{paymentId}/success")
    public ResponseEntity<?> success(@PathVariable Long paymentId) {
        log.info("HTTP POST /api/payment/{}/success", paymentId);
        paymentService.markSuccess(paymentId);
        return ResponseEntity.ok("Payment successful");
    }

    @PostMapping("/{paymentId}/fail")
    public ResponseEntity<?> fail(@PathVariable Long paymentId) {
        log.info("HTTP POST /api/payment/{}/fail", paymentId);
        paymentService.markFailed(paymentId);
        return ResponseEntity.ok("Payment failed");
    }
    @GetMapping("/{paymentId}")
    public PaymentResDto getPaymentById(@PathVariable Long paymentId) {
        log.info("HTTP GET /api/payment/{} - getPaymentById", paymentId);
        return paymentService.getPaymentDetailsBId(paymentId);
    }


}
