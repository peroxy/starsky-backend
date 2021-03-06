package com.starsky.backend.api.user;

import com.starsky.backend.service.UserService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
    @ApiResponse(responseCode = "200", description = "Created a new user successfully.")
    @ApiResponse(responseCode = "400", description = "User info invalid.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        var user = userService.createUser(request);
        return ResponseEntity.ok(user.toResponse());
    }

    @GetMapping("/test")
    public ResponseEntity<String> hello(){
        return ResponseEntity.ok("hello world!");
    }

    @GetMapping("/test-rabbit")
    public Mono<String> testRabbit(){
        var client = WebClient.create("http://mail-api:56789");
        var req = client.post().uri("/invitations").contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue("yolo"));
        var response = req.retrieve();
        return response.bodyToMono(String.class);
    }



}
