package com.starsky.backend.api.schedule;

import io.swagger.v3.oas.annotations.media.Schema;

public class ScheduleUnsolvableResponse {
    @Schema(example = "Schedule could not be solved because it does not have any shifts assigned.", title = "Error describing the issue")
    private final String error;

    public ScheduleUnsolvableResponse(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
