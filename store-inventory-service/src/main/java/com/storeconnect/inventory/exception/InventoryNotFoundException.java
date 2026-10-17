package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class InventoryNotFoundException extends StoreConnectException {
    public InventoryNotFoundException(String storeId, String productId) {
        super(String.format("No inventory record found for store '%s' and product '%s'", storeId, productId), HttpStatus.NOT_FOUND);
    }
}
