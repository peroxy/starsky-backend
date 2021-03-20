package com.starsky.backend.api.invite;

import com.starsky.backend.service.invite.InviteService;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
public class InviteController {

    private final InviteService inviteService;
    private final UserService userService;

    @Autowired
    public InviteController(InviteService inviteService, UserService userService) {
        this.inviteService = inviteService;
        this.userService = userService;
    }

    @PostMapping("/invites")
    @ApiResponse(responseCode = "200", description = "Created a new invite successfully.")
    @ApiResponse(responseCode = "400", description = "Invite body invalid.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateInviteRequest request) {

        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var manager = userService.getUserByEmail(email);

        var invite = inviteService.createInvite(manager, request);
        return ResponseEntity.ok().build();
    }

}
