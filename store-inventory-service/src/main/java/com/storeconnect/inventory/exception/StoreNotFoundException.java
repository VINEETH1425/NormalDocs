package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class StoreNotFoundException extends StoreConnectException {
    public StoreNotFoundException(String storeId) {
        super("Store not found with ID or code: " + storeId, HttpStatus.NOT_FOUND);
    }
}
