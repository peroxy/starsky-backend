package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse {
    @Schema(example = "1", title = "User id")
    private final long id;
    @Schema(example = "David Starsky", title = "User name")
    private final String name;
    @Schema(example = "david@example.com", title = "User email")
    private final String email;
    @JsonProperty("job_title")
    @Schema(example = "Police detective", title = "User job title")
    private final String jobTitle;
    @JsonProperty("phone_number")
    @Schema(example = "+38641891123", title = "User phone number")
    private final String phoneNumber;
    @JsonProperty("notification_type")
    @Schema(example = "EMAIL", title = "User notification preference")
    private final String notificationType;
    @Schema(example = "EMPLOYEE", title = "User role")
    private final String role;
    @Schema(example = "false", title = "Manually added")
    @JsonProperty("manually_added")
    private final boolean manuallyAdded;

    public UserResponse(long id, String name, String email, String jobTitle, String phoneNumber, String notificationType, String role, boolean manuallyAdded) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.notificationType = notificationType;
        this.role = role;
        this.manuallyAdded = manuallyAdded;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getRole() {
        return role;
    }

    public boolean isManuallyAdded() {
        return manuallyAdded;
    }
}
