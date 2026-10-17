package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends StoreConnectException {
    public InvalidCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
