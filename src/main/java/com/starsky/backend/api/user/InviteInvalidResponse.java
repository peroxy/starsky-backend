package com.starsky.backend.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;

public class InviteInvalidResponse {
    @JsonProperty("invite_token")
    @Schema(example = "acaa86b2-ce32-4911-89b8-e1e2a1d39a01", title = "Invalid token")
    @NotNull
    private String inviteToken;
    @Schema(example = "This invite token has expired.", title = "Error describing the invite token issue")
    @NotNull
    private String error;

    public InviteInvalidResponse(String inviteToken, String error) {
        this.inviteToken = inviteToken;
        this.error = error;
    }

    public String getInviteToken() {
        return inviteToken;
    }

    public void setInviteToken(String inviteToken) {
        this.inviteToken = inviteToken;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
