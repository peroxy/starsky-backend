package com.starsky.backend.api.schedule.availability;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EmployeeAvailabilityResponse {
    @Schema(example = "1")
    private final long id;
    @JsonProperty("shift_start")
    @Schema(example = "1617032176.7171679", title = "Epoch timestamp of availability start", implementation = Double.class)
    private final Instant availabilityStart;
    @JsonProperty("shift_end")
    @Schema(example = "1617052176.7171679", title = "Epoch timestamp of availability end", implementation = Double.class)
    private final Instant availabilityEnd;
    @JsonProperty("max_hours_per_shift")
    @Schema(example = "8", title = "Maximum hours per shift")
    private final int maxHoursPerShift;
    @NotNull
    @JsonProperty("employee_id")
    @Schema(example = "1")
    private final long employeeId;

    public EmployeeAvailabilityResponse(Long id, Instant availabilityStart, Instant availabilityEnd, int maxHoursPerShift, long employeeId) {
        this.id = id;
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.maxHoursPerShift = maxHoursPerShift;
        this.employeeId = employeeId;
    }

    public long getId() {
        return id;
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

    public long getEmployeeId() {
        return employeeId;
    }
}
