package com.starsky.backend.api.schedule;

import com.starsky.backend.api.BaseController;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(value = "/user/schedules", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Schedule", description = "Endpoints for schedule management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleController extends BaseController {
    private final ScheduleService scheduleService;

    public ScheduleController(UserService userService, ScheduleService scheduleService) {
        super(userService);
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @Operation(summary = "Get all schedules", description = "Returns a list of all schedules created by the currently authenticated user (manager-role only route).")
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

    @GetMapping("/{schedule_id}")
    @Operation(summary = "Get schedule by id", description = "Returns a schedule with specified id that was created by the currently authenticated user (manager-role only route).")
    @ApiResponse(responseCode = "200", description = "Response with the schedule.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ScheduleResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable("schedule_id") long scheduleId) {
        var user = getAuthenticatedUser();
        var schedule = scheduleService.getSchedule(scheduleId, user);
        return ResponseEntity.ok(schedule.toResponse());
    }
}
