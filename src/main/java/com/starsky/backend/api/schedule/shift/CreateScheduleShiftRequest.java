package com.starsky.backend.api.schedule.shift;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CreateScheduleShiftRequest {
    @NotNull
    @JsonProperty("shift_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of shift start", implementation = Double.class)
    private final Instant shiftStart;
    @NotNull
    @JsonProperty("shift_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of shift end", implementation = Double.class)
    private final Instant shiftEnd;
    @NotNull
    @JsonProperty("number_of_required_employees")
    @Schema(example = "5", title = "Number of required employees for the entire shift")
    @Min(1)
    private final int numberOfRequiredEmployees;

    public CreateScheduleShiftRequest(@NotNull Instant shiftStart, @NotNull Instant shiftEnd, @Min(value = 1) int numberOfRequiredEmployees) {
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.numberOfRequiredEmployees = numberOfRequiredEmployees;
    }

    public Instant getShiftStart() {
        return shiftStart;
    }

    public Instant getShiftEnd() {
        return shiftEnd;
    }

    public int getNumberOfRequiredEmployees() {
        return numberOfRequiredEmployees;
    }
}
