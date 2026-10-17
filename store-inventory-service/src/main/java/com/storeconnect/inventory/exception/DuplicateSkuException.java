package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class DuplicateSkuException extends StoreConnectException {
    public DuplicateSkuException(String sku) {
        super("A product with SKU '" + sku + "' already exists", HttpStatus.CONFLICT);
    }
}
