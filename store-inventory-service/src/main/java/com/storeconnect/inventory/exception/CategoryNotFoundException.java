package com.storeconnect.inventory.exception;

import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends StoreConnectException {
    public CategoryNotFoundException(String categoryId) {
        super("Category not found with ID: " + categoryId, HttpStatus.NOT_FOUND);
    }
}
