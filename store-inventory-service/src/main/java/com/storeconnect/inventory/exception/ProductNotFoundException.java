package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends StoreConnectException {
    public ProductNotFoundException(String productId) {
        super("Product not found with ID or SKU: " + productId, HttpStatus.NOT_FOUND);
    }
}
