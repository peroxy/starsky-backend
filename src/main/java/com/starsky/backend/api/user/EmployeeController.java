package com.starsky.backend.api.user;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.domain.User;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user/employees", produces = MediaType.APPLICATION_JSON_VALUE)
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Employee", description = "Endpoints for employee management")
public class EmployeeController extends BaseController {

    private final UserService userService;

    @Autowired
    public EmployeeController(UserService userService) {
        super(userService);
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated manager's employees", description = "Returns the currently authenticated user's employees - manager only route.")
    @ApiResponse(responseCode = "200", description = "Response with all employees.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    public ResponseEntity<UserResponse[]> getEmployees() {
        var manager = getAuthenticatedUser();
        var employees = userService.getEmployees(manager).stream().map(User::toResponse).toArray(UserResponse[]::new);
        return ResponseEntity.ok(employees);
    }
}
