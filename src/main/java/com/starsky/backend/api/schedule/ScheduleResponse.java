package com.starsky.backend.api.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class ScheduleResponse {
    @Schema(example = "1", title = "Schedule id")
    @NotNull
    private final long id;
    @JsonProperty("schedule_name")
    @Schema(example = "Next week's schedule", title = "Schedule name")
    @NotNull
    private final String scheduleName;
    @JsonProperty("schedule_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of schedule start", implementation = Double.class)
    @NotNull
    private final Instant scheduleStart;
    @JsonProperty("schedule_end")
    @Schema(example = "1617102176.7171679", title = "Epoch timestamp of schedule end", implementation = Double.class)
    @NotNull
    private final Instant scheduleEnd;
    @JsonProperty("team_id")
    @Schema(example = "1", title = "Schedule is assigned to this team")
    @NotNull
    private final long teamId;
    @JsonProperty("max_hours_per_employee")
    @Schema(example = "40", title = "Maximum allowed hours per employee for the duration of the entire schedule")
    @NotNull
    private final int maxHoursPerEmployee;
    @JsonProperty("max_shifts_per_employee")
    @Schema(example = "5", title = "Maximum allowed shifts per employee for the duration of the entire schedule")
    @NotNull
    private final int maxShiftsPerEmployee;
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum allowed hours per shift")
    @NotNull
    private final int maxHoursPerShift;

    public ScheduleResponse(long id, String scheduleName, Instant scheduleStart, Instant scheduleEnd, long teamId, int maxHoursPerEmployee, int maxShiftsPerEmployee, int maxHoursPerShift) {
        this.id = id;
        this.scheduleName = scheduleName;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.teamId = teamId;
        this.maxHoursPerEmployee = maxHoursPerEmployee;
        this.maxShiftsPerEmployee = maxShiftsPerEmployee;
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public long getId() {
        return id;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public Instant getScheduleStart() {
        return scheduleStart;
    }

    public Instant getScheduleEnd() {
        return scheduleEnd;
    }

    public long getTeamId() {
        return teamId;
    }

    public int getMaxHoursPerEmployee() {
        return maxHoursPerEmployee;
    }

    public int getMaxShiftsPerEmployee() {
        return maxShiftsPerEmployee;
    }

    public int getMaxHoursPerShift() {
        return maxHoursPerShift;
    }
}
