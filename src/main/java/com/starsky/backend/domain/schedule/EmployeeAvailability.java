package com.starsky.backend.domain.schedule;

import com.starsky.backend.api.schedule.availability.EmployeeAvailabilityResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class EmployeeAvailability extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee-availability-id-generator")
    @SequenceGenerator(name = "employee-availability-id-generator", sequenceName = "employee_availability_sequence")
    private Long id;
    @OneToOne
    @NotNull
    private User employee;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private ScheduleShift shift;
    @NotNull
    private Instant availabilityStart;
    @NotNull
    private Instant availabilityEnd;
    @NotNull
    private int maxHoursPerShift;
    @Transient
    private ShiftDate shiftDate;

    public EmployeeAvailability(@NotNull User employee,
                                @NotNull ScheduleShift shift,
                                @NotNull Instant availabilityStart,
                                @NotNull Instant availabilityEnd,
                                @NotNull int maxHoursPerShift) {
        this.employee = employee;
        this.shift = shift;
        this.availabilityStart = availabilityStart;
        this.availabilityEnd = availabilityEnd;
        this.maxHoursPerShift = maxHoursPerShift;
        this.shiftDate = new ShiftDate(availabilityStart, availabilityEnd);
    }

    public EmployeeAvailability() {
    }

    @PostLoad
    private void onLoad() {
        this.shiftDate = new ShiftDate(availabilityStart, availabilityEnd);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public ScheduleShift getShift() {
        return shift;
    }

    public void setShift(ScheduleShift shift) {
        this.shift = shift;
    }

    public Instant getAvailabilityStart() {
        return availabilityStart;
    }

    public void setAvailabilityStart(Instant start) {
        this.availabilityStart = start;
    }

    public Instant getAvailabilityEnd() {
        return availabilityEnd;
    }

    public void setAvailabilityEnd(Instant end) {
        this.availabilityEnd = end;
    }

    public int getMaxHoursPerShift() {
        return maxHoursPerShift;
    }

    public void setMaxHoursPerShift(int maxHoursPerShift) {
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public EmployeeAvailabilityResponse toResponse() {
        return new EmployeeAvailabilityResponse(id, availabilityStart, availabilityEnd, maxHoursPerShift, getEmployee().getId());
    }

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }
}
