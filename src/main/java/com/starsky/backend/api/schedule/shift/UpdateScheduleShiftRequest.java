package com.starsky.backend.api.schedule.shift;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.util.Optional;

public class UpdateScheduleShiftRequest {
    @JsonProperty("shift_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of shift start", implementation = Double.class)
    private final Instant shiftStart;
    @JsonProperty("shift_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of shift end", implementation = Double.class)
    private final Instant shiftEnd;
    @JsonProperty("number_of_required_employees")
    @Schema(example = "5", title = "Number of required employees for the entire shift")
    @Min(1)
    private final Integer numberOfRequiredEmployees;

    public UpdateScheduleShiftRequest(Instant shiftStart, Instant shiftEnd, Integer numberOfRequiredEmployees) {
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.numberOfRequiredEmployees = numberOfRequiredEmployees;
    }

    public Optional<Instant> getShiftStart() {
        return Optional.ofNullable(shiftStart);
    }

    public Optional<Instant> getShiftEnd() {
        return Optional.ofNullable(shiftEnd);
    }

    public Optional<Integer> getNumberOfRequiredEmployees() {
        return Optional.ofNullable(numberOfRequiredEmployees);
    }
}
