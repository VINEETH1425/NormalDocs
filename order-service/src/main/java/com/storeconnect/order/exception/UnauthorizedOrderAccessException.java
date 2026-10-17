package com.storeconnect.order.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedOrderAccessException extends StoreConnectException {
    public UnauthorizedOrderAccessException(String orderId) {
        super("You are not authorized to view or manage order: " + orderId, HttpStatus.FORBIDDEN);
    }
}
