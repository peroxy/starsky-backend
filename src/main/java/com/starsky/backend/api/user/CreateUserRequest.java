package com.starsky.backend.api.user;

import com.starsky.backend.domain.User;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CreateUserRequest {

    public CreateUserRequest(String name, String email, String password, String jobTitle) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
    }

    @NotBlank
    @Size(max=256)
    private String name;

    @Email
    @NotBlank
    private String email;

    @Size(min=8, max=71)
    private String password;

    @NotBlank
    @Size(max=256)
    private String jobTitle;

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
}
