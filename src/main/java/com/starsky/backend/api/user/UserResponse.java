package com.starsky.backend.api.user;

public class UserResponse {

    public UserResponse(long id, String name, String email, String jobTitle, String phoneNumber, String notificationType, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.notificationType = notificationType;
        this.role = role;
    }

    private final long id;

    private final String name;

    private final String email;

    private final String jobTitle;

    private final String phoneNumber;

    private final String notificationType;

    private final String role;

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
