package com.starsky.backend.api.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class ScheduleUnsolvableResponse {
    @Schema(example = "Schedule could not be solved because it does not have any shifts assigned.", title = "Error describing the issue")
    @NotNull
    private final String error;

    public ScheduleUnsolvableResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
