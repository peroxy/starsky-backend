package com.starsky.backend.api.schedule.shift;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.exception.ForbiddenResponse;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.service.schedule.shift.ScheduleShiftService;
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
import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE, value = "/user")
@Tag(name = "Schedule Shift", description = "Endpoints for schedule shift management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleShiftController extends BaseController {

    private final ScheduleShiftService scheduleShiftService;

    @Autowired
    public ScheduleShiftController(UserService userService, ScheduleShiftService scheduleShiftService) {
        super(userService);
        this.scheduleShiftService = scheduleShiftService;
    }

    @GetMapping("/schedules/{schedule_id}/shifts")
    @Operation(summary = "Get all schedule shifts", description = "Returns a list of all schedule shifts. " +
            "Managers may access all schedule shifts, while employees will need to be in the specified schedule's team to access this resource.")
    @ApiResponse(responseCode = "200", description = "Response with a list of schedule shifts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScheduleShiftResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have necessary permissions to access the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForbiddenResponse.class)))
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<ScheduleShiftResponse[]> getScheduleShifts(@PathVariable(value = "schedule_id") long scheduleId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var scheduleShifts = scheduleShiftService.getScheduleShifts(scheduleId, user).stream().map(ScheduleShift::toResponse).toArray(ScheduleShiftResponse[]::new);
        return ResponseEntity.ok(scheduleShifts);
    }

    @GetMapping("/shifts/{shift_id}")
    @Operation(summary = "Get schedule shift", description = "Returns the specified schedule shifts. " +
            "Managers may access all schedule shifts, while employees will need to be in the specified schedule's team to access this resource.")
    @ApiResponse(responseCode = "200", description = "Response with the schedule shifts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleShiftResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have necessary permissions to access the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ForbiddenResponse.class)))
    @ApiResponse(responseCode = "404", description = "Shift does not exist.", content = @Content)
    public ResponseEntity<ScheduleShiftResponse> getScheduleShift(@PathVariable(value = "shift_id") long shiftId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var scheduleShift = scheduleShiftService.getScheduleShift(shiftId, user).toResponse();
        return ResponseEntity.ok(scheduleShift);
    }

    @PostMapping("/schedules/{schedule_id}/shifts")
    @Operation(summary = "Create a new schedule shift", description = "Creates a new schedule shift that is assigned to the specified schedule. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created a new schedule shift successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleShiftResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid schedule shift date range (start timestamp occurs after end timestamp).", content = @Content)
    public ResponseEntity<ScheduleShiftResponse> postScheduleShift(@Valid @RequestBody CreateScheduleShiftRequest request, @PathVariable(value = "schedule_id") long scheduleId) throws DateRangeException, ForbiddenException {
        var user = getAuthenticatedUser();
        var schedule = scheduleShiftService.createScheduleShift(scheduleId, request, user);
        return ResponseEntity.ok(schedule.toResponse());
    }

    @DeleteMapping("/shifts/{shift_id}")
    @Operation(summary = "Delete schedule shift", description = "Delete a schedule shift. This will also cascade delete employee availabilities." +
            " Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted the schedule shift successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Shift does not exist.", content = @Content)
    public ResponseEntity<Void> deleteScheduleShift(@PathVariable("shift_id") long shiftId) {
        var user = getAuthenticatedUser();
        scheduleShiftService.deleteScheduleShift(shiftId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/shifts/{shift_id}")
    @Operation(summary = "Update schedule shift", description = "Update any property of the specified shift. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Updated the schedule shift successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleShiftResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule shift does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid date range (start timestamp occurs after end timestamp) supplied.", content = @Content)
    public ResponseEntity<ScheduleShiftResponse> patchScheduleShift(@PathVariable("shift_id") long shiftId, @Valid @RequestBody UpdateScheduleShiftRequest request) throws DateRangeException {
        var user = getAuthenticatedUser();
        var schedule = scheduleShiftService.updateScheduleShift(shiftId, request, user);
        return ResponseEntity.ok(schedule.toResponse());
    }

    @PutMapping("/schedules/{schedule_id}/shifts")
    @Operation(summary = "Create or update multiple schedule shifts", description = "Creates or updates schedule shifts. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "List of created/updates schedule shifts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScheduleShiftResponse.class))))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Shift or schedule does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description =
            "Invalid shift date range, start timestamp occurs after end timestamp, date range exists or overlaps with existing shift..", content = @Content)
    public ResponseEntity<ScheduleShiftResponse[]> putScheduleShifts(@Valid @RequestBody List<CreateScheduleShiftRequest> shifts, @PathVariable("schedule_id") long scheduleId) throws ForbiddenException, DateRangeException {
        var user = getAuthenticatedUser();
        var scheduleShifts = scheduleShiftService.putAll(shifts, user, scheduleId);
        return ResponseEntity.ok(scheduleShifts.stream().map(ScheduleShift::toResponse).toArray(ScheduleShiftResponse[]::new));
    }
}
