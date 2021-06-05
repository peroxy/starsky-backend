package com.starsky.backend.domain.schedule;

import com.starsky.backend.api.schedule.shift.ScheduleShiftResponse;
import com.starsky.backend.domain.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Entity
public class ScheduleShift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule-shift-id-generator")
    @SequenceGenerator(name = "schedule-shift-id-generator", sequenceName = "schedule_shift_sequence")
    private Long id;
    @NotNull
    private Instant shiftStart;
    @NotNull
    private Instant shiftEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Schedule schedule;
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeAvailability> employeeAvailabilities;
    @OneToMany(mappedBy = "shift", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EmployeeAssignment> employeeAssignments;
    @NotNull
    private int numberOfRequiredEmployees;

    public ScheduleShift(@NotNull Instant shiftStart,
                         @NotNull Instant shiftEnd,
                         @NotNull Schedule schedule,
                         @NotNull int numberOfRequiredEmployees) {
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.schedule = schedule;
        this.numberOfRequiredEmployees = numberOfRequiredEmployees;
    }

    public ScheduleShift() {
    }

    public Long getId() {
        return id;
    }

    public Instant getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(Instant start) {
        this.shiftStart = start;
    }

    public Instant getShiftEnd() {
        return shiftEnd;
    }

    public void setShiftEnd(Instant end) {
        this.shiftEnd = end;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public int getNumberOfRequiredEmployees() {
        return numberOfRequiredEmployees;
    }

    public void setNumberOfRequiredEmployees(int numberOfRequiredEmployees) {
        this.numberOfRequiredEmployees = numberOfRequiredEmployees;
    }

    public ScheduleShiftResponse toResponse() {
        return new ScheduleShiftResponse(id, shiftStart, shiftEnd, numberOfRequiredEmployees);
    }

    public List<EmployeeAvailability> getEmployeeAvailabilities() {
        return employeeAvailabilities;
    }

    public List<EmployeeAssignment> getEmployeeAssignments() {
        return employeeAssignments;
    }
}
