package com.starsky.backend.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class ForbiddenResponse {
    @Schema(example = "Currently authenticated user does not have necessary permissions to access this resource.", title = "Error describing the issue")
    @NotNull
    private final String error;

    public ForbiddenResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}