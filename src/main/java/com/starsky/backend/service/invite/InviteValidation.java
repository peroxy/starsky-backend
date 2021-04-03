package com.starsky.backend.service.invite;

public class InviteValidation {
    private final String error;
    private final boolean hasError;

    public InviteValidation(String error, boolean hasError) {
        this.error = error;
        this.hasError = hasError;
    }

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return hasError;
    }
}
