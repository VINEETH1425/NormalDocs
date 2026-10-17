package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class InsufficientStockException extends StoreConnectException {
    public InsufficientStockException(String productId, int requested, int available) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d", productId, requested, available), HttpStatus.CONFLICT);
    }
}
