package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.starsky.backend.annotation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import java.util.Optional;

public class UpdateEmployeeRequest {
    @NullOrNotBlank
    @Size(max = 256)
    @Schema(example = "David Starsky", title = "Employee's name")
    private String name;
    @Email
    @NullOrNotBlank
    @Schema(example = "david@example.com", title = "Employee's email")
    private String email;
    @NullOrNotBlank
    @Size(max = 256)
    @JsonProperty("job_title")
    @Schema(example = "Police detective", title = "Employee's job title")
    private String jobTitle;

    public UpdateEmployeeRequest(String name, String email, String jobTitle) {
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Optional<String> getJobTitle() {
        return Optional.ofNullable(jobTitle);
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
