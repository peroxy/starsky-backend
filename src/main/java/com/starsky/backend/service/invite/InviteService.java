package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;

import java.util.UUID;

public interface InviteService {
    Invite createInvite(User manager, CreateInviteRequest request);
    Invite findByToken(UUID token);
    Invite updateInvite(Invite invite);
}
