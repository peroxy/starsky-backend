package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CreateEmployeeAssignmentRequest {
    @NotNull
    @JsonProperty("assignment_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of assignment start", implementation = Double.class)
    private final Instant assignmentStart;
    @NotNull
    @JsonProperty("assignment_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of assignment end", implementation = Double.class)
    private final Instant assignmentEnd;

    public CreateEmployeeAssignmentRequest(Instant assignmentStart, Instant assignmentEnd) {
        this.assignmentStart = assignmentStart;
        this.assignmentEnd = assignmentEnd;
    }

    public Instant getAssignmentEnd() {
        return assignmentEnd;
    }

    public Instant getAssignmentStart() {
        return assignmentStart;
    }
}
