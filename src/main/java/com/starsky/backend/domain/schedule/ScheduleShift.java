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
    private Instant start;
    @NotNull
    private Instant end;
    @OneToOne
    @NotNull
    private Schedule schedule;
    @NotNull
    private int numberOfRequiredEmployees;

    public ScheduleShift(@NotNull Instant start,
                         @NotNull Instant end,
                         @NotNull Schedule schedule,
                         @NotNull int numberOfRequiredEmployees) {
        this.start = start;
        this.end = end;
        this.schedule = schedule;
        this.numberOfRequiredEmployees = numberOfRequiredEmployees;
    }

    public ScheduleShift() {
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
