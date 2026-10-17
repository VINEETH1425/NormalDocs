package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class InvalidQuantityException extends StoreConnectException {
    public InvalidQuantityException(int quantity) {
        super("Invalid item quantity: " + quantity + ". Quantity must be greater than 0.", HttpStatus.BAD_REQUEST);
    }
}
