package com.starsky.backend.api.schedule.shift;

import com.starsky.backend.api.BaseController;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.service.schedule.ScheduleShiftService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user/schedules/{schedule_id}/shifts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Schedule Shift", description = "Endpoints for schedule shift management")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleShiftController extends BaseController {

    private final ScheduleShiftService scheduleShiftService;

    @Autowired
    public ScheduleShiftController(UserService userService, ScheduleShiftService scheduleShiftService) {
        super(userService);
        this.scheduleShiftService = scheduleShiftService;
    }

    @GetMapping
    @Operation(summary = "Get all schedule shifts", description = "Returns a list of all schedule shifts created by the currently authenticated user (manager-role only route).")
    @ApiResponse(responseCode = "200", description = "Response with a list of schedule shifts.",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = ScheduleShiftResponse.class))))
    @ApiResponse(responseCode = "403", description = "Forbidden, user is not authenticated or does not have manager role.", content = @Content)
    @ApiResponse(responseCode = "404", description = "Schedule does not exist.", content = @Content)
    public ResponseEntity<ScheduleShiftResponse[]> getScheduleShifts(@PathVariable(value = "schedule_id") long scheduleId) {
        var user = getAuthenticatedUser();
        var scheduleShifts = scheduleShiftService.getScheduleShifts(scheduleId, user).stream().map(ScheduleShift::toResponse).toArray(ScheduleShiftResponse[]::new);
        return ResponseEntity.ok(scheduleShifts);
    }
}
