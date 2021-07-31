package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EmployeeAssignmentResponse {
    @Schema(example = "1")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long id;
    @NotNull
    @JsonProperty("assignment_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of assignment start", implementation = Double.class)
    private final Instant assignmentStart;
    @NotNull
    @JsonProperty("assignment_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of assignment end", implementation = Double.class)
    private final Instant assignmentEnd;
    @NotNull
    @JsonProperty("employee_id")
    @Schema(example = "1")
    private final long employeeId;
    @NotNull
    @JsonProperty("shift_id")
    @Schema(example = "3")
    private final long shiftId;

    public EmployeeAssignmentResponse(Long id, Instant assignmentStart, Instant assignmentEnd, long employeeId, long shiftId) {
        this.assignmentStart = assignmentStart;
        this.assignmentEnd = assignmentEnd;
        this.employeeId = employeeId;
        this.shiftId = shiftId;
        this.id = id;
    }

    public Instant getAssignmentStart() {
        return assignmentStart;
    }

    public Instant getAssignmentEnd() {
        return assignmentEnd;
    }

    public long getEmployeeId() {
        return employeeId;
    }

    public long getShiftId() {
        return shiftId;
    }

    public Long getId() {
        return id;
    }
}
