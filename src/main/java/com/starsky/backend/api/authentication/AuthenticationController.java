package com.starsky.backend.api.authentication;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Endpoint for login.")
public class AuthenticationController {

    @PostMapping(value = "/login")
    @Operation(summary = "Login an existing user", description = "Login an existing user with their email and password. " +
            "The API will return a newly created JWT Bearer token with its expiry date.")
    @ApiResponse(responseCode = "200", description = "Successfully logged in.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = TokenResponse.class)))
    @ApiResponse(responseCode = "401", description = "Login failed.")
    public void login(@Valid @RequestBody LoginRequest loginRequest) {
        throw new IllegalStateException("This is actually implemented by Spring Security, this method is only used for OpenAPI generation.");
    }
}

