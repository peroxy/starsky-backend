package com.starsky.backend.api.team;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;


public class CreateTeamRequest {
    @NotBlank
    @Schema(example = "My Police Squad", title = "Team name")
    private String name;

    @JsonCreator
    public CreateTeamRequest(@NotBlank String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }
}
