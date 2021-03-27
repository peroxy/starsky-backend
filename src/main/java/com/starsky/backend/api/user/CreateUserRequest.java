package com.starsky.backend.api.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

public class CreateUserRequest {

    @NotBlank
    @Size(max = 256)
    private String name;
    @Email
    @NotBlank
    private String email;
    @Size(min = 8, max = 71)
    private String password;
    @NotBlank
    @Size(max = 256)
    private String jobTitle;
    private UUID inviteToken;

    public CreateUserRequest(@NotBlank @Size(max = 256) String name,
                             @Email @NotBlank String email,
                             @Size(min = 8, max = 71) String password,
                             @NotBlank @Size(max = 256) String jobTitle,
                             UUID inviteToken) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
        this.inviteToken = inviteToken;
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

    public UUID getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(UUID inviteToken) {
        this.inviteToken = inviteToken;
    }
}
