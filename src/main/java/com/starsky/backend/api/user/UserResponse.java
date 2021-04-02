package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public class UserResponse {


    @Schema(example = "1", title = "User's id")
    private final long id;
    @Schema(example = "David Starsky", title = "User's name")
    private final String name;
    @Schema(example = "david@example.com", title = "User's email")
    private final String email;
    @JsonProperty("job_title")
    @Schema(example = "Police detective", title = "User's job title")
    private final String jobTitle;
    @JsonProperty("phone_number")
    @Schema(example = "+38641891123", title = "User's phone number")
    private final String phoneNumber;
    @JsonProperty("notification_type")
    @Schema(example = "EMAIL", title = "User's notification preference")
    private final String notificationType;
    @Schema(example = "EMPLOYEE", title = "User's role")
    private final String role;

    public UserResponse(long id, String name, String email, String jobTitle, String phoneNumber, String notificationType, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.notificationType = notificationType;
        this.role = role;
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

}
