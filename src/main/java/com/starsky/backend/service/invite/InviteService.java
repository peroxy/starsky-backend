package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface InviteService {
    Invite createInvite(User manager, CreateInviteRequest request);

    ResponseEntity<Void> sendInviteToMailApi(User manager, CreateInviteRequest request, Invite invite);

    Invite findByToken(UUID token);

    Invite updateInvite(Invite invite);

    InviteValidation validateInvite(Invite invite);

    List<Invite> getAllManagerInvites(User manager);
}
