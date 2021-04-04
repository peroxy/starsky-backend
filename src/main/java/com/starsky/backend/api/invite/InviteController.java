package com.starsky.backend.api.invite;

import com.starsky.backend.api.BaseController;
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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/user/invites", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Invite", description = "Endpoints for user invite management")
@SecurityRequirement(name = "bearerAuth")
public class InviteController extends BaseController {

    private final InviteService inviteService;

    @Autowired
    public InviteController(UserService userService, InviteService inviteService) {
        super(userService);
        this.inviteService = inviteService;
    }

    @PostMapping
    @Operation(summary = "Send a new invite",
            description = "Send an invite email to the specified employee so they can create a new Starsky account and join the manager's team. " +
                    "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created a new invite successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InviteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invite body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<InviteResponse> createInvite(@Valid @RequestBody CreateInviteRequest request) {
        var manager = getAuthenticatedUser();
        var invite = inviteService.createInvite(manager, request);
        return ResponseEntity.ok(invite.toResponse());
    }

    @GetMapping
    @Operation(summary = "Get all sent invites", description = "Returns the currently authenticated user's invites. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Response with a list of invites.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InviteResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    public ResponseEntity<InviteResponse[]> getInvites() {
        var user = getAuthenticatedUser();
        var invites = inviteService.getAllManagerInvites(user).stream().map(Invite::toResponse).toArray(InviteResponse[]::new);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/{invite_id}")
    @Operation(summary = "Get sent invite", description = "Returns the invite by id. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Response with invite.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InviteResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Invite does not exist.", content = @Content)
    public ResponseEntity<InviteResponse> getInviteById(@PathVariable("invite_id") long inviteId) {
        var invite = inviteService.getById(inviteId);
        return invite.map(value -> ResponseEntity.ok(value.toResponse())).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
