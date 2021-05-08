package com.starsky.backend.api.schedule.availability;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class CreateEmployeeAvailabilityRequest {
    @NotNull
    @JsonProperty("availability_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of availability start", implementation = Double.class)
    private final Instant availabilityStart;
    @NotNull
    @JsonProperty("availability_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of availability end", implementation = Double.class)
    private final Instant availabilityEnd;
    @NotNull
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum hours per shift")
    @Min(1)
    private final int maxHoursPerShift;
    @NotNull
    @JsonProperty("employee_id")
    @Schema(example = "1")
    private final int employeeId;

    public CreateEmployeeAvailabilityRequest(Instant availabilityStart, Instant availabilityEnd, int maxHoursPerShift, int employeeId) {
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.maxHoursPerShift = maxHoursPerShift;
        this.employeeId = employeeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public Instant getAvailabilityStart() {
        return availabilityStart;
    }

    public Instant getAvailabilityEnd() {
        return availabilityEnd;
    }

    public int getMaxHoursPerShift() {
        return maxHoursPerShift;
    }
}
