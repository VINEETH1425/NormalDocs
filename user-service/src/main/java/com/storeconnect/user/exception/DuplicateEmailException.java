package com.storeconnect.user.exception;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends StoreConnectException {
    public DuplicateEmailException(String email) {
        super("An account with email '" + email + "' already exists", HttpStatus.CONFLICT);
    }
}
