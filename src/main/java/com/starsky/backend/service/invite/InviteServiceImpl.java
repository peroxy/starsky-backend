package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.api.invite.CreateMailApiInviteRequest;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.InviteRepository;
import com.starsky.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import javax.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InviteServiceImpl implements InviteService {

    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final String mailApiHostname;
    private final String frontendRegisterUrl;

    private final Logger logger = LoggerFactory.getLogger(InviteServiceImpl.class);

    @Autowired
    public InviteServiceImpl(UserRepository userRepository, InviteRepository inviteRepository,
                             @Value("${starsky.mail-api.host}") String mailApiHostname,
                             @Value("${starsky.frontend.register-url}") String frontendRegisterUrl) {
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.mailApiHostname = mailApiHostname;
        this.frontendRegisterUrl = frontendRegisterUrl;
    }

    @Override
    public Invite createInvite(User manager, CreateInviteRequest request) {
        var existingInvite = inviteRepository.findByEmployeeEmail(request.getEmployeeEmail());
        var existingUser = userRepository.findByEmailAndEnabled(request.getEmployeeEmail(), true);
        if (existingInvite != null || existingUser.isPresent()) {
            var error = "Key (employee email)=(%s) already exists.".formatted(request.getEmployeeEmail());
            this.logger.warn(error);
            throw new DataIntegrityViolationException(error);
        }

        var token = UUID.randomUUID();
        var invite = new Invite(token, manager, request.getEmployeeName(), request.getEmployeeEmail(), false);
        invite = inviteRepository.save(invite);

        sendInviteToMailApi(manager, request, invite);

        return invite;
    }

    @Override
    public ResponseEntity<Void> sendInviteToMailApi(User manager, CreateInviteRequest request, Invite invite) {
        var url = UriComponentsBuilder.fromHttpUrl(frontendRegisterUrl)
                .queryParam("token", invite.getToken())
                .queryParam("manager", manager.getName())
                .queryParam("name", request.getEmployeeName())
                .queryParam("email", request.getEmployeeEmail())
                .build().encode().toUriString();

        var body = new CreateMailApiInviteRequest(manager.getName(), request.getEmployeeName(), request.getEmployeeEmail(), url);
        this.logger.info("Sending request to mail-api: {}", body);
        var client = WebClient.create(mailApiHostname);
        var response = client.post().uri("/invitations").contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().toBodilessEntity().block();
        //4xx and 5xx errors are thrown and then the transaction gets rollbacked
        this.logger.info("Response code from starsky mail-api: {}", response.getStatusCode());
        return response;
    }

    @Override
    public Invite getByToken(UUID token) {
        return inviteRepository.findByToken(token);
    }

    @Override
    public Optional<Invite> getById(long id, User manager) {
        return inviteRepository.findByIdAndManager(id, manager);
    }

    @Override
    public Invite updateInvite(Invite invite) {
        return inviteRepository.save(invite);
    }

    @Override
    public InviteValidation validateInvite(Invite invite) {
        if (invite == null) {
            return new InviteValidation("Invite token does not exist.", true);
        }
        if (invite.getHasRegistered()) {
            return new InviteValidation("Invite has already been used, user has already been registered.", true);
        }
        if (Duration.between(invite.getUpdatedAt(), Instant.now()).toDays() > 3) {
            return new InviteValidation("Invite has expired - all invites have expiry date of 3 days.", true);
        }
        return new InviteValidation("No error.", false);
    }

    @Override
    public List<Invite> getAllManagerInvites(User manager) {
        return inviteRepository.findAllByManager(manager);
    }
}
