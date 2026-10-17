package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class CartItemNotFoundException extends StoreConnectException {
    public CartItemNotFoundException(String itemId) {
        super("Cart item not found with ID: " + itemId, HttpStatus.NOT_FOUND);
    }
}
