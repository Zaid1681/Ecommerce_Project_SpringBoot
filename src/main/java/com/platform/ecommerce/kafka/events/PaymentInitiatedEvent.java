package com.platform.ecommerce.kafka.events;

import java.time.LocalDateTime;

public class PaymentInitiatedEvent {
    private Long orderId;
    private Long paymentId;
    private Long userId;
    private String email;
    private String status;
    private Double amount;
    private String paymentIntentId;
    private LocalDateTime createdAt;

    public PaymentInitiatedEvent() {}

    public PaymentInitiatedEvent(Long orderId, Long paymentId, Long userId, String email,
                                 String status, Double amount, String paymentIntentId,
                                 LocalDateTime createdAt) {
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.amount = amount;
        this.paymentIntentId = paymentIntentId;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
