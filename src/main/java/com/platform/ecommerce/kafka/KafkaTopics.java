package com.platform.ecommerce.kafka;

public final class KafkaTopics {

    private KafkaTopics() {}

    public static final String ORDER_CONFIRMED = "order-confirmed";
    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_SUCCEEDED = "payment-succeeded";
    public static final String PAYMENT_FAILED = "payment-failed";
}
