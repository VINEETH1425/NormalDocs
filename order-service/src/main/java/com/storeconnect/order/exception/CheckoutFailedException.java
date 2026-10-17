package com.storeconnect.order.exception;

import org.springframework.http.HttpStatus;

public class CheckoutFailedException extends StoreConnectException {
    public CheckoutFailedException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
