package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.starsky.backend.annotation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Optional;

public class UpdateUserRequest {
    @Size(max = 256)
    @Schema(example = "David Starsky", title = "User's name")
    @NullOrNotBlank
    private final String name;
    @Email
    @Schema(example = "david@example.com", title = "User's email")
    @NullOrNotBlank
    private final String email;
    @Size(min = 8, max = 71)
    @Schema(example = "password")
    @NullOrNotBlank
    private final String password;
    @Size(max = 256)
    @JsonProperty("job_title")
    @Schema(example = "Police detective", title = "User's job title")
    @NullOrNotBlank
    private final String jobTitle;

    public UpdateUserRequest(String name, String email, String password, String jobTitle) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public Optional<String> getJobTitle() {
        return Optional.ofNullable(jobTitle);
    }
}
