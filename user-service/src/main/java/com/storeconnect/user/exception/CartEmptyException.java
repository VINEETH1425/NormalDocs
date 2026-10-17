package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class CartEmptyException extends StoreConnectException {
    public CartEmptyException(String customerId) {
        super("Cart is empty for customer: " + customerId, HttpStatus.BAD_REQUEST);
    }
}
