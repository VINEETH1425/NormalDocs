package com.storeconnect.order.exception;

import org.springframework.http.HttpStatus;

public class OrderNotFoundException extends StoreConnectException {
    public OrderNotFoundException(String orderId) {
        super("Order not found with ID or order number: " + orderId, HttpStatus.NOT_FOUND);
    }
}
