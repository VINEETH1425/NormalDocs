package com.storeconnect.order.exception;

import com.storeconnect.order.entity.OrderStatus;
import org.springframework.http.HttpStatus;

public class InvalidStatusTransitionException extends StoreConnectException {
    public InvalidStatusTransitionException(OrderStatus current, OrderStatus target) {
        super(String.format("Invalid order status transition from '%s' to '%s'", current, target), HttpStatus.BAD_REQUEST);
    }
}
