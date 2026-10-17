package com.storeconnect.order.exception;

import org.springframework.http.HttpStatus;

public class StoreConnectException extends RuntimeException {

    private final HttpStatus status;

    public StoreConnectException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
