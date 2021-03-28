package com.starsky.backend.api.user;

import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "User", description = "Endpoints for user information, new user registration...")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            var user = userService.createUser(request);
            return ResponseEntity.ok(user.toResponse());
        } catch (IllegalArgumentException ex) {
            var error = new InviteInvalidResponse(request.getInviteToken().toString(), ex.getMessage());
            return ResponseEntity.unprocessableEntity().body(error);
        }
    }

    @GetMapping("/user")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get the authenticated user", description = "Returns the currently authenticated user's information.")
    @ApiResponse(responseCode = "200", description = "Response with user information.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "403", description = "Unauthorized, user is not authenticated.", content = @Content)
    public ResponseEntity<UserResponse> getUser() {
        var email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user.toResponse());
    }

}

