package com.starsky.backend.api.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class InvalidDateRangeResponse {
    @Schema(example = "The start date occurs after end date.", title = "Error describing the date issue")
    @NotNull
    private final String error;

    public InvalidDateRangeResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
