package com.starsky.backend.api.schedule.availability;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import java.time.Instant;
import java.util.Optional;

public class UpdateEmployeeAvailabilityRequest {
    @JsonProperty("availability_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of availability start", implementation = Double.class)
    private final Instant availabilityStart;
    @JsonProperty("availability_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of availability end", implementation = Double.class)
    private final Instant availabilityEnd;
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum hours per shift")
    @Min(1)
    private final Integer maxHoursPerShift;

    public UpdateEmployeeAvailabilityRequest(Instant availabilityStart, Instant availabilityEnd, Integer maxHoursPerShift) {
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public Optional<Instant> getAvailabilityStart() {
        return Optional.ofNullable(availabilityStart);
    }

    public Optional<Instant> getAvailabilityEnd() {
        return Optional.ofNullable(availabilityEnd);
    }

    public Optional<Integer> getMaxHoursPerShift() {
        return Optional.ofNullable(maxHoursPerShift);
    }
}
