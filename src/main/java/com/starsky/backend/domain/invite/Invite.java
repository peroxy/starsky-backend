package com.starsky.backend.domain.invite;

import com.starsky.backend.api.invite.InviteResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.UUID;

@Entity
public class Invite extends BaseEntity {

    private static final Duration expireAfter = Duration.ofDays(3);
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invite-id-generator")
    @SequenceGenerator(name = "invite-id-generator", sequenceName = "invite_sequence")
    private long id;
    @NotNull
    private UUID token;
    @OneToOne
    @NotNull
    private User manager;
    @NotNull
    private String employeeName;
    @NotNull
    @Column(unique = true)
    private String employeeEmail;
    @NotNull
    private boolean hasRegistered;

    public Invite(@NotNull UUID token,
                  @NotNull User manager,
                  @NotNull String employeeName,
                  @NotNull String employeeEmail,
                  @NotNull boolean hasRegistered) {
        this.token = token;
        this.manager = manager;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.hasRegistered = hasRegistered;
    }

    public Invite() {
    }

    public long getId() {
        return id;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
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

    public boolean getHasRegistered() {
        return hasRegistered;
    }

    public void setHasRegistered(boolean hasRegistered) {
        this.hasRegistered = hasRegistered;
    }

    public UUID getToken() {
        return token;
    }

    public InviteResponse toResponse() {
        return new InviteResponse(id, employeeName, employeeEmail, hasRegistered, getUpdatedAt().plus(expireAfter), expireAfter.getSeconds());
    }
}
