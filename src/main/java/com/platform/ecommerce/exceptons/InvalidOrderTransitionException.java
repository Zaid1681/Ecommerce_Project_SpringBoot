package com.platform.ecommerce.exceptons;

public class InvalidOrderTransitionException extends RuntimeException {
    public InvalidOrderTransitionException(String msg) {
        super(msg);
    }
}