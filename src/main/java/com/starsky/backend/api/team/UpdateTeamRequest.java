package com.starsky.backend.api.team;

import com.starsky.backend.annotation.NullOrNotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public class UpdateTeamRequest {
    @NullOrNotBlank
    @Schema(example = "My Police Squad", title = "Team name")
    private final String name;

    public UpdateTeamRequest(String name) {
        this.name = name;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
