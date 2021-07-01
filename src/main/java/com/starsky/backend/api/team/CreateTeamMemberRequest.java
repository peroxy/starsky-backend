package com.starsky.backend.api.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class CreateTeamMemberRequest {
    @NotNull
    @JsonProperty("employee_id")
    @Schema(example = "1", title = "Employee's user ID")
    private long employeeId;

    public CreateTeamMemberRequest() {
    }

    public CreateTeamMemberRequest(long employeeId) {
        this.employeeId = employeeId;
    }

    public long getEmployeeId() {
        return employeeId;
    }
}
