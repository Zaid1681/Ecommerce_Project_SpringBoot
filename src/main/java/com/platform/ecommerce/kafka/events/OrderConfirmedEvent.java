package com.platform.ecommerce.kafka.events;

import java.time.LocalDateTime;

public class OrderConfirmedEvent {
    private Long orderId;
    private Long userId;
    private String email;
    private String status;
    private Double totalAmount;
    private LocalDateTime createdAt;

    public OrderConfirmedEvent() {}

    public OrderConfirmedEvent(Long orderId, Long userId, String email,
                               String status, Double totalAmount, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
