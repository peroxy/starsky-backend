package com.starsky.backend.api.invite;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class InviteResponse {
    @Schema(example = "1", title = "ID of the created invite")
    private final long id;

    @JsonProperty("employee_name")
    @Schema(example = "Kenneth Hutchinson", title = "Invited employee's name")
    private final String employeeName;

    @JsonProperty("employee_email")
    @Schema(example = "kenneth@example.com", title = "Invited employee's email address")
    private final String employeeEmail;

    @JsonProperty("has_registered")
    @Schema(example = "false", title = "Has the employee accepted the invite and registered yet")
    private final boolean hasRegistered;

    @JsonProperty("expires_on")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of the invite expiry date", implementation = Double.class)
    private final Instant expiresOn;

    @JsonProperty("expires_in")
    @Schema(example = "259200", title = "Invite expires in x seconds")
    private final long expiresIn;

    public InviteResponse(long id, String employeeName, String employeeEmail, boolean hasRegistered, Instant expiresOn, long expiresIn) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.hasRegistered = hasRegistered;
        this.expiresOn = expiresOn;
        this.expiresIn = expiresIn;
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

    public boolean getHasRegistered() {
        return hasRegistered;
    }

    public Instant getExpiresOn() {
        return expiresOn;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}
