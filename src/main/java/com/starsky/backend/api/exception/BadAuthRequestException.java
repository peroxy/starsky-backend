package com.starsky.backend.api.exception;

import org.springframework.security.core.AuthenticationException;

public class BadAuthRequestException extends AuthenticationException {
    public BadAuthRequestException(String msg) {
        super(msg);
    }
}
