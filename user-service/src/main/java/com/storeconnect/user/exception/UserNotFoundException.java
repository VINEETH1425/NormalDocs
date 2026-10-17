package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends StoreConnectException {
    public UserNotFoundException(String identifier) {
        super("User not found with ID or email: " + identifier, HttpStatus.NOT_FOUND);
    }
}
