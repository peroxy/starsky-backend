package com.starsky.backend.api.authentication;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @ApiResponse(responseCode = "200", description = "Successfully logged in.")
    @ApiResponse(responseCode = "401", description = "Login failed.")
    @PostMapping(value = "/login")
    public void login(@Valid @RequestBody LoginRequest loginRequest) {
        throw new IllegalStateException("This is actually implemented by Spring Security, this method is only used for OpenAPI generation.");
    }
}

