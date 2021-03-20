package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;

public interface InviteService {
    Invite createInvite(User manager, CreateInviteRequest request);
}
