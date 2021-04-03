package com.starsky.backend.api.exception;

import java.util.UUID;

public class InvalidInviteTokenException extends Exception {
    private final UUID inviteToken;

    public InvalidInviteTokenException(UUID inviteToken, String error) {
        super(error);
        this.inviteToken = inviteToken;
    }

    public UUID getInviteToken() {
        return inviteToken;
    }

}
