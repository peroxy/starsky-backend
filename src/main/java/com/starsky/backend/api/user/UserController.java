package com.starsky.backend.api.user;

import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
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

}

