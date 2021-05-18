package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InviteService {
    Invite createInvite(User manager, CreateInviteRequest request);

    ResponseEntity<Void> sendInviteToMailApi(User manager, CreateInviteRequest request, Invite invite);

    Invite getByToken(UUID token);

    Optional<Invite> getById(long id, User manager);

    Invite updateInvite(Invite invite);

    InviteValidation validateInvite(Invite invite);

    List<Invite> getAllManagerInvites(User manager);

    void deleteInvite(long id, User manager) throws ResourceNotFoundException;
}
