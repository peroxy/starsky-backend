package com.starsky.backend.api.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class TokenResponse {

    @Schema(example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...", title = "JWT access token")
    @JsonProperty("access_token")
    @NotNull
    private String accessToken;

    @Schema(example = "Bearer", title = "Type of JWT token")
    @JsonProperty("token_type")
    @NotNull
    private String tokenType;

    @Schema(example = "1617032176.7171679", title = "Epoch timestamp", implementation = Double.class)
    @JsonProperty("expires_on")
    @NotNull
    private Instant expiresOn;

    @Schema(example = "86400", title = "Expires in x seconds")
    @JsonProperty("expires_in")
    @NotNull
    private long expiresIn;

    public TokenResponse(String accessToken, String tokenType, Instant expiresOn, long expiresIn) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresOn = expiresOn;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Instant getExpiresOn() {
        return expiresOn;
    }

    public void setExpiresOn(Instant expiresOn) {
        this.expiresOn = expiresOn;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
