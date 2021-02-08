package com.starsky.backend.api;

import com.starsky.backend.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users/")
    @ApiResponse(responseCode = "200", description = "Created a new user successfully.")
    @ApiResponse(responseCode = "400", description = "User info invalid.", content = @Content)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        var user = userService.createUser(request);
        return ResponseEntity.ok(user.toResponse());
    }

}