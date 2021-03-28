package com.starsky.backend.api.invite;

import com.starsky.backend.domain.Invite;
import com.starsky.backend.service.invite.InviteService;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Invite", description = "Endpoints for invite operations.")
public class InviteController {

    private final InviteService inviteService;
    private final UserService userService;

    @Autowired
    public InviteController(InviteService inviteService, UserService userService) {
        this.inviteService = inviteService;
        this.userService = userService;
    }

    @PostMapping("/invites")
    @Operation(summary = "Send a new invite",
            description = "Send an invite email to the specified employee so they can create a new Starsky account and join the manager's team. " +
                    "Must be logged in with a manager role account.")
    @ApiResponse(responseCode = "200", description = "Created a new invite successfully.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Invite body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Unauthorized, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<?> createInvite(@Valid @RequestBody CreateInviteRequest request) {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var manager = userService.getUserByEmail(email);
        var invite = inviteService.createInvite(manager, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invites")
    @Operation(summary = "Get all sent invites", description = "Returns the currently authenticated user's invites. User must have the manager role.")
    @ApiResponse(responseCode = "200", description = "Response with invites.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InviteResponse.class))))
    @ApiResponse(responseCode = "403", description = "Unauthorized, user is not authenticated or does not have manager role.", content = @Content)
    public ResponseEntity<InviteResponse[]> getInvites() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userService.getUserByEmail(email);
        var invites = inviteService.getAllManagerInvites(user).stream().map(Invite::toResponse).toArray(InviteResponse[]::new);
        return ResponseEntity.ok(invites);
    }

}
