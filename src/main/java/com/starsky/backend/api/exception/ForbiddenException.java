package com.starsky.backend.api.exception;

public class ForbiddenException extends Exception {
    public ForbiddenException(String message) {
        super(message);
    }
}
