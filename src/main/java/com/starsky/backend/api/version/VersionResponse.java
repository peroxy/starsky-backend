package com.starsky.backend.api.version;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class VersionResponse {
    @Schema(example = "1.0.0", title = "Current version")
    @NotNull
    private String version;

    public VersionResponse() {
    }

    public VersionResponse(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
