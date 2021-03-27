package com.starsky.backend.service.invite;

public class InviteValidation {
    public InviteValidation(String error, boolean hasError) {
        this.error = error;
        this.hasError = hasError;
    }

    private String error;
    private boolean hasError;

    public String getError() {
        return error;
    }

    public boolean hasError() {
        return hasError;
    }
}
