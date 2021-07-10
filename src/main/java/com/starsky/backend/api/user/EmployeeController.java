package com.starsky.backend.api.user;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.domain.user.User;
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

    @PostMapping
    @Operation(summary = "Manually create a new employee", description = "Manually create a new employee for the currently authenticated user - manager only route. " +
            "This employee will not be able to login - employees should be invited if they want to access the platform and register themselves. " +
            "This is used when a manager wants to add employees that don't necessarily need platform access, but he still needs to create schedules.")
    @ApiResponse(responseCode = "200", description = "Response with newly created employee.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<UserResponse> postEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        var manager = getAuthenticatedUser();
        var employee = userService.createEmployee(request, manager);
        return ResponseEntity.ok(employee.toResponse());
    }

    @PatchMapping("/{employee_id}")
    @Operation(summary = "Update an existing employee", description = "Update an existing employee's properties - manager only route. ")
    @ApiResponse(responseCode = "200", description = "Response with updated employee.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have the manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Employee does not exist.", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists.", content = @Content)
    public ResponseEntity<UserResponse> patchEmployee(@Valid @RequestBody UpdateEmployeeRequest request, @PathVariable("employee_id") long employeeId) {
        var manager = getAuthenticatedUser();
        var employee = userService.updateEmployee(request, manager, employeeId);
        return ResponseEntity.ok(employee.toResponse());
    }
}
