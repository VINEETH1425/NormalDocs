package com.storeconnect.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentAlreadyProcessedException extends StoreConnectException {
    public PaymentAlreadyProcessedException(String orderId) {
        super("Payment has already been processed for order: " + orderId, HttpStatus.CONFLICT);
    }
}
