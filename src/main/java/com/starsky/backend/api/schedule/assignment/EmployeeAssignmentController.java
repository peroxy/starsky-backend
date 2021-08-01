package com.starsky.backend.api.schedule.assignment;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.service.schedule.assignment.EmployeeAssignmentService;
import com.starsky.backend.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/user/schedules/{schedule_id}")
@Tag(name = "Employee assignment", description = "Endpoints for schedule shift employee assignments")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class EmployeeAssignmentController extends BaseController {
    private final EmployeeAssignmentService employeeAssignmentService;

    public EmployeeAssignmentController(UserService userService, EmployeeAssignmentService employeeAssignmentService) {
        super(userService);
        this.employeeAssignmentService = employeeAssignmentService;
    }

    @PutMapping("/assignments")
    @Operation(summary = "Create or update employee assignments", description = "Creates or updates a schedule with all of the specified schedule employee assignments. " +
            "Please note that this operation can be destructive - it will always delete all of the previous/existing employee assignments (if they exist) for the specified schedule and create or update with the new ones. " +
            "Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Created employee assignments successfully.", content = @Content)
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule, shift or employee does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description =
            "Invalid employee assignment date range, start timestamp occurs after end timestamp, date range exists or overlaps with existing assignment..", content = @Content)
    public ResponseEntity<Void> putEmployeeAssignment(@Valid @RequestBody List<PutEmployeeAssignmentRequest> requests, @PathVariable(value = "schedule_id") long scheduleId) throws ForbiddenException, DateRangeException {
        var user = getAuthenticatedUser();
        employeeAssignmentService.putAll(requests, scheduleId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/shifts/{shift_id}/employees/{employee_id}/assignments")
    @Operation(summary = "Create employee assignment", description = "Creates a schedule employee assignments. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created employee assignments successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeAssignmentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Shift or employee does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description =
            "Invalid employee assignment date range, start timestamp occurs after end timestamp, date range exists or overlaps with existing assignment..", content = @Content)
    public ResponseEntity<EmployeeAssignmentResponse> postEmployeeAssignment(@Valid @RequestBody CreateEmployeeAssignmentRequest request,
                                                                             @PathVariable(value = "schedule_id") long scheduleId,
                                                                             @PathVariable(value = "employee_id") long employeeId,
                                                                             @PathVariable(value = "shift_id") long shiftId) throws ForbiddenException, DateRangeException, ResourceNotFoundException {
        var user = getAuthenticatedUser();
        var assignment = employeeAssignmentService.create(scheduleId, shiftId, employeeId, user, request);
        return ResponseEntity.ok(assignment.toResponse());
    }

    @PatchMapping("/assignments/{assignment_id}")
    @Operation(summary = "Update employee assignment", description = "Updates a schedule employee assignment. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Updated employee assignment successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EmployeeAssignmentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule or assignment does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description =
            "Invalid employee assignment date range, start timestamp occurs after end timestamp,..", content = @Content)
    public ResponseEntity<EmployeeAssignmentResponse> patchEmployeeAssignment(@Valid @RequestBody UpdateEmployeeAssignmentRequest request,
                                                                              @PathVariable(value = "schedule_id") long scheduleId,
                                                                              @PathVariable(value = "assignment_id") long assignmentId)
            throws ForbiddenException, DateRangeException {
        var user = getAuthenticatedUser();
        var assignment = employeeAssignmentService.update(assignmentId, scheduleId, user, request);
        return ResponseEntity.ok(assignment.toResponse());
    }

    @DeleteMapping("/assignments/{assignment_id}")
    @Operation(summary = "Delete employee assignment", description = "Deletes a schedule employee assignment. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted employee assignment successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule or assignment does not exist.", content = @Content)
    public ResponseEntity<Void> deleteEmployeeAssignment(@PathVariable(value = "schedule_id") long scheduleId, @PathVariable(value = "assignment_id") long assignmentId) throws ForbiddenException, DateRangeException {
        var user = getAuthenticatedUser();
        employeeAssignmentService.delete(assignmentId, scheduleId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignments")
    @Operation(summary = "Get employee assignments", description = "Get all of the employee assignments for the specified schedule. ")
    @ApiResponse(responseCode = "200", description = "Response with a list of employee assignments.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = EmployeeAssignmentResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have correct permissions.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<EmployeeAssignmentResponse[]> getEmployeeAssignments(@PathVariable(value = "schedule_id") long scheduleId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var assignments = employeeAssignmentService.getAll(scheduleId, user)
                .stream().map(EmployeeAssignment::toResponse)
                .toArray(EmployeeAssignmentResponse[]::new);
        return ResponseEntity.ok(assignments);
    }

}
