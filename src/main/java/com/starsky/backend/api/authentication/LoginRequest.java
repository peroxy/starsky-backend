package com.starsky.backend.api.authentication;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class LoginRequest {

    @NotNull
    @Email
    @Schema(example = "mail@example.com", title = "E-mail address")
    private String email;
    @NotNull
    @Size(min = 8, max = 71)
    @Schema(example = "password")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
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
}
