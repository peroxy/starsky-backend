package com.starsky.backend.api.schedule.availability;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.exception.ForbiddenResponse;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.service.schedule.availability.EmployeeAvailabilityService;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/user")
@Tag(name = "Employee availability", description = "Endpoints for schedule employee availability management")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeAvailabilityController extends BaseController {
    private final EmployeeAvailabilityService employeeAvailabilityService;

    public EmployeeAvailabilityController(UserService userService, EmployeeAvailabilityService employeeAvailabilityService) {
        super(userService);
        this.employeeAvailabilityService = employeeAvailabilityService;
    }

    @GetMapping("/shifts/{shift_id}/availabilities")
    @Operation(summary = "Get all employee availabilities", description = "Returns a list of all employee availabilities. " +
            "Managers may access all employee availabilities, while employees will need to be in the specified schedule's team to access this resource.")
    @ApiResponse(responseCode = "200", description = "Response with a list of employee availabilities.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = EmployeeAvailabilityResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have necessary permissions to access the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForbiddenResponse.class)))
    @ApiResponse(responseCode = "404", description = "Shift does not exist.", content = @Content)
    public ResponseEntity<EmployeeAvailabilityResponse[]> getEmployeeAvailabilities(@PathVariable(value = "shift_id") long shiftId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var availabilities =
                employeeAvailabilityService.getEmployeeAvailabilities(shiftId, user)
                        .stream().map(EmployeeAvailability::toResponse)
                        .toArray(EmployeeAvailabilityResponse[]::new);
        return ResponseEntity.ok(availabilities);
    }

    @GetMapping("/availabilities/{availability_id}")
    @Operation(summary = "Get employee availability", description = "Returns the specified employee availability. " +
            "Managers may access all employee availabilities, while employees will need to be in the specified schedule's team to access this resource.")
    @ApiResponse(responseCode = "200", description = "Response with the employee availability.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeAvailabilityResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have necessary permissions to access the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForbiddenResponse.class)))
    @ApiResponse(responseCode = "404", description = "Employee availability does not exist.", content = @Content)
    public ResponseEntity<EmployeeAvailabilityResponse> getEmployeeAvailability(@PathVariable(value = "availability_id") long availabilityId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var availability = employeeAvailabilityService.getEmployeeAvailability(availabilityId, user).toResponse();
        return ResponseEntity.ok(availability);
    }

    @PostMapping("/shifts/{shift_id}/availabilities")
    @Operation(summary = "Create a new employee availability", description = "Creates a new employee availability that is assigned to the specified schedule shift. " +
            "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created a new employee availability successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeAvailabilityResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Shift does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid employee availability date range (start timestamp occurs after end timestamp).", content = @Content)
    public ResponseEntity<EmployeeAvailabilityResponse> createEmployeeAvailability(@Valid @RequestBody CreateEmployeeAvailabilityRequest request,
                                                                                   @PathVariable(value = "shift_id") long shiftId) throws ForbiddenException, DateRangeException {
        var user = getAuthenticatedUser();
        var employeeAvailability = employeeAvailabilityService.createEmployeeAvailability(shiftId, request, user);
        return ResponseEntity.ok(employeeAvailability.toResponse());
    }

    @DeleteMapping("/availabilities/{availability_id}")
    @Operation(summary = "Delete employee availability", description = "Delete an employee availability. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted the employee availability successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Employee availability does not exist.", content = @Content)
    public ResponseEntity<Void> deleteEmployeeAvailability(@PathVariable("availability_id") long availabilityId) {
        var user = getAuthenticatedUser();
        employeeAvailabilityService.deleteEmployeeAvailability(availabilityId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/availabilities/{availability_id}")
    @Operation(summary = "Update employee availability", description = "Update any property (except actual employee) of the specified employee availability. " +
            "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Updated the employee availability successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeAvailabilityResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Employee availability does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid date range (start timestamp occurs after end timestamp).", content = @Content)
    public ResponseEntity<EmployeeAvailabilityResponse> updateEmployeeAvailability(@PathVariable("availability_id") long availabilityId,
                                                                                   @Valid @RequestBody UpdateEmployeeAvailabilityRequest request) throws DateRangeException {
        var user = getAuthenticatedUser();
        var employeeAvailability = employeeAvailabilityService.updateEmployeeAvailability(availabilityId, request, user);
        return ResponseEntity.ok(employeeAvailability.toResponse());
    }
}
