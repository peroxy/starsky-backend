package com.starsky.backend.api.schedule;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.util.Optional;

public class UpdateScheduleRequest {
    @JsonProperty("schedule_name")
    @Schema(example = "Next week's schedule", title = "Schedule name")
    private final String scheduleName;
    @JsonProperty("schedule_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of schedule start", implementation = Double.class)
    private final Instant scheduleStart;
    @JsonProperty("schedule_end")
    @Schema(example = "1617102176.7171679", title = "Epoch timestamp of schedule end", implementation = Double.class)
    private final Instant scheduleEnd;
    @JsonProperty("max_hours_per_employee")
    @Schema(example = "40", title = "Maximum allowed hours per employee for the duration of the entire schedule")
    @Min(0)
    private final Integer maxHoursPerEmployee;
    @JsonProperty("max_shifts_per_employee")
    @Schema(example = "5", title = "Maximum allowed shifts per employee for the duration of the entire schedule")
    @Min(0)
    private final Integer maxShiftsPerEmployee;
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum allowed hours per shift")
    @Min(0)
    private final Integer maxHoursPerShift;

    public UpdateScheduleRequest(String scheduleName, Instant scheduleStart, Instant scheduleEnd, Integer maxHoursPerEmployee, Integer maxShiftsPerEmployee, Integer maxHoursPerShift) {
        this.scheduleName = scheduleName;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.maxHoursPerEmployee = maxHoursPerEmployee;
        this.maxShiftsPerEmployee = maxShiftsPerEmployee;
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public Optional<String> getScheduleName() {
        return Optional.ofNullable(scheduleName);
    }

    public Optional<Instant> getScheduleStart() {
        return Optional.ofNullable(scheduleStart);
    }

    public Optional<Instant> getScheduleEnd() {
        return Optional.ofNullable(scheduleEnd);
    }

    public Optional<Integer> getMaxHoursPerEmployee() {
        return Optional.ofNullable(maxHoursPerEmployee);
    }

    public Optional<Integer> getMaxShiftsPerEmployee() {
        return Optional.ofNullable(maxShiftsPerEmployee);
    }

    public Optional<Integer> getMaxHoursPerShift() {
        return Optional.ofNullable(maxHoursPerShift);
    }
}

