package com.starsky.backend.domain.schedule;

import com.starsky.backend.domain.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class ScheduleShift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule-shift-id-generator")
    @SequenceGenerator(name = "schedule-shift-id-generator", sequenceName = "schedule_shift_sequence", allocationSize = 1)
    private Long id;
    @NotNull
    private Instant shiftStart;
    @NotNull
    private Instant shiftEnd;
    @OneToOne
    @NotNull
    private Schedule schedule;
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
}
