package com.starsky.backend.api.schedule;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.service.schedule.ScheduleService;
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
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Schedule", description = "Endpoints for schedule management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController extends BaseController {
    private final ScheduleService scheduleService;

    @Autowired
    public ScheduleController(UserService userService, ScheduleService scheduleService) {
        super(userService);
        this.scheduleService = scheduleService;
    }

    @GetMapping("/user/schedules")
    @Operation(summary = "Get all schedules", description = "Returns a list of all schedules created by the currently authenticated user." +
            " Optionally you can filter by team by supplying the query parameter.")
    @ApiResponse(responseCode = "200", description = "Response with a list of schedules.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScheduleResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    public ResponseEntity<ScheduleResponse[]> getSchedules(@RequestParam(value = "team_id", required = false) Optional<Long> teamId) {
        var user = getAuthenticatedUser();
        ScheduleResponse[] schedules;
        if (teamId.isPresent()) {
            schedules = scheduleService.getSchedulesByTeam(user, teamId.get()).stream().map(Schedule::toResponse).toArray(ScheduleResponse[]::new);
        } else {
            schedules = scheduleService.getSchedules(user).stream().map(Schedule::toResponse).toArray(ScheduleResponse[]::new);
        }
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/user/schedules/{schedule_id}")
    @Operation(summary = "Get schedule by id", description = "Returns a schedule with specified id. " +
            "Managers can get all schedules they created, while employees may only get schedules from their team.")
    @ApiResponse(responseCode = "200", description = "Response with the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have sufficient permissions.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable("schedule_id") long scheduleId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        var schedule = scheduleService.getSchedule(scheduleId, user);
        return ResponseEntity.ok(schedule.toResponse());
    }

    @PostMapping("/user/teams/{team_id}/schedules")
    @Operation(summary = "Create a new schedule", description = "Creates a new schedule that is assigned to the specified team. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Created a new schedule successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Team does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid schedule date range (start timestamp occurs after end timestamp) supplied.", content = @Content)
    public ResponseEntity<ScheduleResponse> createSchedule(@Valid @RequestBody CreateScheduleRequest request, @PathVariable("team_id") long teamId) throws DateRangeException {
        var user = getAuthenticatedUser();
        var schedule = scheduleService.createSchedule(request, teamId, user);
        return ResponseEntity.ok(schedule.toResponse());
    }

    @DeleteMapping("/user/schedules/{schedule_id}")
    @Operation(summary = "Delete schedule", description = "Delete a specified schedule. This will also cascade delete schedule shifts and employee availabilities." +
            " Authenticated user must have manager role.")
    @ApiResponse(responseCode = "204", description = "Deleted the schedule successfully.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<Void> deleteSchedule(@PathVariable("schedule_id") long scheduleId) throws ForbiddenException {
        var user = getAuthenticatedUser();
        scheduleService.deleteSchedule(scheduleId, user);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/user/schedules/{schedule_id}")
    @Operation(summary = "Update schedule", description = "Update any property of the specified schedule. Authenticated user must have manager role.")
    @ApiResponse(responseCode = "200", description = "Updated the schedule successfully.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "400", description = "Request body invalid.", content = @Content)
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    @ApiResponse(responseCode = "422", description = "Invalid schedule date range (start timestamp occurs after end timestamp) supplied.", content = @Content)
    public ResponseEntity<ScheduleResponse> updateSchedule(@PathVariable("schedule_id") long scheduleId, @Valid @RequestBody UpdateScheduleRequest request) throws DateRangeException, ForbiddenException {
        var user = getAuthenticatedUser();
        var schedule = scheduleService.updateSchedule(request, scheduleId, user);
        return ResponseEntity.ok(schedule.toResponse());
    }
}
