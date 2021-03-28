package com.starsky.backend.api.invite;

import java.time.Instant;

public class InviteResponse {
    private final long id;
    private final String employeeName;
    private final String employeeEmail;
    private final boolean hasRegistered;
    private final Instant expiresOn;

    public InviteResponse(long id, String employeeName, String employeeEmail, boolean hasRegistered, Instant expiresOn) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.hasRegistered = hasRegistered;
        this.expiresOn = expiresOn;
    }

    public long getId() {
        return id;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public boolean isHasRegistered() {
        return hasRegistered;
    }

    public Instant getExpiresOn() {
        return expiresOn;
    }
}
