package com.starsky.backend.api.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CreateScheduleRequest {
    @NotNull
    @JsonProperty("schedule_name")
    @Schema(example = "Next week's schedule", title = "Schedule name")
    private final String scheduleName;
    @NotNull
    @JsonProperty("schedule_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of schedule start", implementation = Double.class)
    private final Instant scheduleStart;
    @NotNull
    @JsonProperty("schedule_end")
    @Schema(example = "1617102176.7171679", title = "Epoch timestamp of schedule end", implementation = Double.class)
    private final Instant scheduleEnd;
    @JsonProperty("max_hours_per_employee")
    @NotNull
    @Schema(example = "40", title = "Maximum allowed hours per employee for the duration of the entire schedule")
    @Min(0)
    private final int maxHoursPerEmployee;
    @NotNull
    @JsonProperty("max_shifts_per_employee")
    @Schema(example = "5", title = "Maximum allowed shifts per employee for the duration of the entire schedule")
    @Min(0)
    private final int maxShiftsPerEmployee;
    @NotNull
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum allowed hours per shift")
    @Min(0)
    private final int maxHoursPerShift;

    public CreateScheduleRequest(String scheduleName, Instant scheduleStart, Instant scheduleEnd, int maxHoursPerEmployee, int maxShiftsPerEmployee, int maxHoursPerShift) {
        this.scheduleName = scheduleName;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.maxHoursPerEmployee = maxHoursPerEmployee;
        this.maxShiftsPerEmployee = maxShiftsPerEmployee;
        this.maxHoursPerShift = maxHoursPerShift;
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
