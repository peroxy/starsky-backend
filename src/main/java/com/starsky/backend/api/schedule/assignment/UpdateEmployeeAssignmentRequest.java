package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Optional;

public class UpdateEmployeeAssignmentRequest {
    @JsonProperty("assignment_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of employee assignment start date", implementation = Double.class)
    private final Instant assignmentStart;
    @JsonProperty("assignment_end")
    @Schema(example = "1617102176.7171679", title = "Epoch timestamp of employee assignment end date", implementation = Double.class)
    private final Instant assignmentEnd;

    public UpdateEmployeeAssignmentRequest(Instant assignmentStart, Instant assignmentEnd) {
        this.assignmentStart = assignmentStart;
        this.assignmentEnd = assignmentEnd;
    }

    public Optional<Instant> getAssignmentStart() {
        return Optional.ofNullable(assignmentStart);
    }

    public Optional<Instant> getAssignmentEnd() {
        return Optional.ofNullable(assignmentEnd);
    }
}
