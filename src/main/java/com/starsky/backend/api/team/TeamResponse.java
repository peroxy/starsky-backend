package com.starsky.backend.api.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

public class TeamResponse {
    @Schema(example = "1", title = "Team id")
    private final long id;
    @Schema(example = "Harold's police squad", title = "Team name")
    private final String name;
    @Schema(example = "Harold C. Dobey", title = "Team owner's name")
    @JsonProperty("owner_name")
    private final String ownerName;

    public TeamResponse(long id, String name, String ownerName) {
        this.id = id;
        this.name = name;
        this.ownerName = ownerName;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOwnerName() {
        return ownerName;
    }
}
