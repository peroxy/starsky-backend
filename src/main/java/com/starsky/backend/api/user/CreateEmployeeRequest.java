package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CreateEmployeeRequest {
    @NotBlank
    @Size(max = 256)
    @Schema(example = "David Starsky", title = "Employee's name")
    private String name;
    @Email
    @NotBlank
    @Schema(example = "david@example.com", title = "Employee's email")
    private String email;
    @NotBlank
    @Size(max = 256)
    @JsonProperty("job_title")
    @Schema(example = "Police detective", title = "Employee's job title")
    private String jobTitle;

    public CreateEmployeeRequest(String name, String email, String jobTitle) {
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
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

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }
}
