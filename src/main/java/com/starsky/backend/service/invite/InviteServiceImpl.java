package com.starsky.backend.service.invite;

import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.api.invite.InvitationsModel;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.InviteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Transactional
public class InviteServiceImpl implements InviteService {

    private final InviteRepository inviteRepository;
    private final String mailApiHostname;

    private final Logger logger = LoggerFactory.getLogger(InviteServiceImpl.class);

    @Autowired
    public InviteServiceImpl(InviteRepository inviteRepository, @Value("${starsky.mail-api.host}") String mailApiHostname) {
        this.inviteRepository = inviteRepository;
        this.mailApiHostname = mailApiHostname;
    }

    @Override
    public Invite createInvite(User manager, CreateInviteRequest request) {
        var existingInvite = inviteRepository.findByEmployeeEmail(request.getEmployeeEmail());
        if (existingInvite != null) {
            var error = "Key (employee email)=(%s) already exists.".formatted(request.getEmployeeEmail());
            this.logger.warn(error);
            throw new DataIntegrityViolationException(error);
        }

        var token = UUID.randomUUID();
        var invite = new Invite(token, manager, request.getEmployeeName(), request.getEmployeeEmail(), false);
        invite = inviteRepository.save(invite);

        var body = new InvitationsModel(manager.getName(), request.getEmployeeName(), request.getEmployeeEmail(), "http://TODO.com/%s".formatted(invite.getToken()));
        this.logger.info("Sending request to mail-api: {}", body);
        var client = WebClient.create(mailApiHostname);
        var response = client.post().uri("/invitations").contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().toBodilessEntity();
        //4xx and 5xx errors are thrown and then the transaction gets rollbacked
        this.logger.info("Response code from starsky mail-api: {}", response.block().getStatusCode());

        return invite;
    }
}
