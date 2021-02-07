package com.starsky.backend.api;

import com.starsky.backend.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(name = "users", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{email}")
    @ApiResponse(responseCode = "200", description = "Found the user with this email.")
    @ApiResponse(responseCode = "404", description = "User with this email does not exist.", content = @Content)
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email){
        var user = userService.getUserByEmail(email);
        if (user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(user.toResponse());
    }

}
