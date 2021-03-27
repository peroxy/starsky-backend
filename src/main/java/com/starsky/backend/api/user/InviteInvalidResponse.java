package com.starsky.backend.api.user;

public class InviteInvalidResponse {
    public InviteInvalidResponse(String inviteToken, String error) {
        this.inviteToken = inviteToken;
        this.error = error;
    }

    private String inviteToken;
    private String error;

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
