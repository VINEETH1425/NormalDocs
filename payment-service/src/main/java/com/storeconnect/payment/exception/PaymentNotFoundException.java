package com.storeconnect.payment.exception;

import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends StoreConnectException {
    public PaymentNotFoundException(String identifier) {
        super("Payment record not found for: " + identifier, HttpStatus.NOT_FOUND);
    }
}
