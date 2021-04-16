package com.starsky.backend.domain.schedule;

import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class EmployeeAvailability extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee-availability-id-generator")
    @SequenceGenerator(name = "employee-availability-id-generator", sequenceName = "employee_availability_sequence", allocationSize = 1)
    private Long id;
    @OneToOne
    @NotNull
    private User employee;
    @OneToOne
    @NotNull
    private ScheduleShift shift;
    @NotNull
    private Instant start;
    @NotNull
    private Instant end;
    @NotNull
    private int maxHoursPerShift;

    public EmployeeAvailability(@NotNull User employee,
                                @NotNull ScheduleShift shift,
                                @NotNull Instant start,
                                @NotNull Instant end,
                                @NotNull int maxHoursPerShift) {
        this.employee = employee;
        this.shift = shift;
        this.start = start;
        this.end = end;
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public EmployeeAvailability() {
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

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }

    public int getMaxHoursPerShift() {
        return maxHoursPerShift;
    }

    public void setMaxHoursPerShift(int maxHoursPerShift) {
        this.maxHoursPerShift = maxHoursPerShift;
    }
}
