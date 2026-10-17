package com.storeconnect.order.exception;

import com.storeconnect.order.entity.OrderStatus;
import org.springframework.http.HttpStatus;

public class OrderNotCancellableException extends StoreConnectException {
    public OrderNotCancellableException(String orderId, OrderStatus status) {
        super(String.format("Order '%s' cannot be cancelled because it is in '%s' status", orderId, status), HttpStatus.BAD_REQUEST);
    }
}
