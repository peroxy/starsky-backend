package com.starsky.backend.domain.user;

import com.starsky.backend.api.user.UserResponse;
import com.starsky.backend.domain.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user-id-generator")
    @SequenceGenerator(name = "user-id-generator", sequenceName = "user_sequence")
    private long id;
    @NotNull
    private String name;
    @NotNull
    @Column(unique = true)
    private String email;
    private String password;
    @NotNull
    private String jobTitle;
    private String phoneNumber;
    @NotNull
    private boolean enabled;
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private NotificationType notificationType;
    @NotNull
    @Enumerated(EnumType.ORDINAL)
    private Role role;
    @OneToOne
    private User parentUser;
    @NotNull
    private boolean manuallyAdded;

    public User() {
    }

    public User(String name,
                String email,
                String password,
                String jobTitle,
                String phoneNumber,
                boolean enabled,
                NotificationType notificationType,
                Role role,
                User parentUser,
                boolean manuallyAdded) {

        this.name = name;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
        this.phoneNumber = phoneNumber;
        this.enabled = enabled;
        this.notificationType = notificationType;
        this.role = role;
        this.parentUser = parentUser;
        this.manuallyAdded = manuallyAdded;
    }

    public boolean isManuallyAdded() {
        return manuallyAdded;
    }

    public void setManuallyAdded(boolean manuallyAdded) {
        this.manuallyAdded = manuallyAdded;
    }

    public UserResponse toResponse() {
        return new UserResponse(getId(), getName(), getEmail(), getJobTitle(), getPhoneNumber(), getNotificationType().name(), getRole().name(), isManuallyAdded());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public User getParentUser() {
        return parentUser;
    }

    public void setParentUser(User parentUser) {
        this.parentUser = parentUser;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}

