package com.starsky.backend.api.invite;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class CreateInviteRequest {

    @NotNull
    @JsonProperty("employee_name")
    @Schema(example = "Kenneth Hutchinson", title = "Employee's name")
    private String employeeName;
    @NotNull
    @Column(unique = true)
    @Email
    @JsonProperty("employee_email")
    @Schema(example = "kenneth@example.com", title = "Employee's email address")
    private String employeeEmail;

    public CreateInviteRequest(@NotNull String employeeName, @NotNull String employeeEmail) {
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }
}
