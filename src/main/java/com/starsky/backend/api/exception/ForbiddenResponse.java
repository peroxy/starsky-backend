package com.starsky.backend.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public class ForbiddenResponse {
    @Schema(example = "Currently authenticated user does not have necessary permissions to access this resource.", title = "Error describing the issue")
    private final String error;

    public ForbiddenResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}