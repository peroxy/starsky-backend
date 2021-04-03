package com.starsky.backend.api.user;

import com.starsky.backend.api.exception.InvalidInviteTokenException;
import com.starsky.backend.api.invite.CreateInviteRequest;
import com.starsky.backend.api.invite.InviteResponse;
import com.starsky.backend.api.team.TeamResponse;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.Team;
import com.starsky.backend.domain.User;
import com.starsky.backend.service.invite.InviteService;
import com.starsky.backend.service.team.TeamService;
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
@Tag(name = "User", description = "Endpoints for user information, new user registration, sending invites to employees...")
public class UserController {

    private final UserService userService;
    private final InviteService inviteService;
    private final TeamService teamService;

    @Autowired
    public UserController(UserService userService, InviteService inviteService, TeamService teamService) {
        this.userService = userService;
        this.inviteService = inviteService;
        this.teamService = teamService;
    }

    @PostMapping("/users")
    @Operation(summary = "Register a new user", description = "You can register a new user with the manager role by only supplying their name, email, password and job title. " +
            "By adding a valid invite token (which the employee receives by mail) to the request body, the newly registered user will have the employee role.")
    @ApiResponse(responseCode = "200", description = "Created a new user successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "User info invalid.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invite token invalid.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InviteInvalidResponse.class)))
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) throws InvalidInviteTokenException {
        var user = userService.createUser(request);
        return ResponseEntity.ok(user.toResponse());
    }

    @GetMapping("/user")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated user", description = "Returns the currently authenticated user's information.")
    @ApiResponse(responseCode = "200", description = "Response with user information.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated.", content = @Content)
    public ResponseEntity<UserResponse> getUser() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user.toResponse());
    }

    @GetMapping("/user/employees")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated manager's employees", description = "Returns the currently authenticated user's employees - manager only route.")
    @ApiResponse(responseCode = "200", description = "Response with all employees.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    public ResponseEntity<UserResponse[]> getEmployees() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var manager = userService.getUserByEmail(email);
        var employees = userService.getEmployees(manager).stream().map(User::toResponse).toArray(UserResponse[]::new);
        return ResponseEntity.ok(employees);
    }

    @PostMapping("/user/invites")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Send a new invite",
            description = "Send an invite email to the specified employee so they can create a new Starsky account and join the manager's team. " +
                    "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created a new invite successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InviteResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invite body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<InviteResponse> createInvite(@Valid @RequestBody CreateInviteRequest request) {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var manager = userService.getUserByEmail(email);
        var invite = inviteService.createInvite(manager, request);
        return ResponseEntity.ok(invite.toResponse());
    }

    @GetMapping("/user/invites")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all sent invites", description = "Returns the currently authenticated user's invites. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Response with a list of invites.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = InviteResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    public ResponseEntity<InviteResponse[]> getInvites() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userService.getUserByEmail(email);
        var invites = inviteService.getAllManagerInvites(user).stream().map(Invite::toResponse).toArray(InviteResponse[]::new);
        return ResponseEntity.ok(invites);
    }

    @GetMapping("/user/invites/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get sent invite", description = "Returns the invite by id. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Response with invite.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = InviteResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Invite does not exist.", content = @Content)
    public ResponseEntity<InviteResponse> getInvite(@PathVariable long id) {
        var invite = inviteService.getById(id);
        return invite.map(value -> ResponseEntity.ok(value.toResponse())).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/teams")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user's teams", description = "Returns the teams the user owns (manager) or the ones he is part of (employee).")
    @ApiResponse(responseCode = "200", description = "Response with a list of teams.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = TeamResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated.", content = @Content)
    public ResponseEntity<TeamResponse[]> getTeams() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userService.getUserByEmail(email);
        var teams = teamService.getTeams(user).stream().map(Team::toResponse).toArray(TeamResponse[]::new);
        return ResponseEntity.ok(teams);
    }

}

