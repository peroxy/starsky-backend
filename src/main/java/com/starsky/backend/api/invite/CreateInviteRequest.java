package com.starsky.backend.api.invite;

import javax.persistence.Column;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class CreateInviteRequest {

    @NotNull
    private String employeeName;
    @NotNull
    @Column(unique = true)
    @Email
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
