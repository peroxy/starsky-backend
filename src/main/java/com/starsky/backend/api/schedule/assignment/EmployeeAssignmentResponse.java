package com.starsky.backend.api.schedule.assignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class EmployeeAssignmentResponse {
    @NotNull
    @Schema(example = "1")
    private final long id;
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

    public EmployeeAssignmentResponse(long id, Instant assignmentStart, Instant assignmentEnd, long employeeId, long shiftId) {
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

    public long getId() {
        return id;
    }
}
