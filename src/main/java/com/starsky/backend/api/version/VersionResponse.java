package com.starsky.backend.api.version;

public class VersionResponse {
    private String version;

    public VersionResponse(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
