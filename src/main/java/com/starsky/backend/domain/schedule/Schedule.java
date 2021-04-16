package com.starsky.backend.domain.schedule;

import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.team.Team;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule-id-generator")
    @SequenceGenerator(name = "schedule-id-generator", sequenceName = "schedule_sequence", allocationSize = 1)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Instant start;
    @NotNull
    private Instant end;
    @OneToOne
    @NotNull
    private Team team;
    @NotNull
    private int maxHoursPerEmployee;
    @NotNull
    private int maxShiftsPerEmployee;
    @NotNull
    private int maxHoursPerShift;

    public Schedule(@NotNull String name,
                    @NotNull Instant start,
                    @NotNull Instant end,
                    @NotNull Team team,
                    @NotNull int maxHoursPerEmployee,
                    @NotNull int maxShiftsPerEmployee,
                    @NotNull int maxHoursPerShift) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.team = team;
        this.maxHoursPerEmployee = maxHoursPerEmployee;
        this.maxShiftsPerEmployee = maxShiftsPerEmployee;
        this.maxHoursPerShift = maxHoursPerShift;
    }

    public Schedule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getMaxHoursPerEmployee() {
        return maxHoursPerEmployee;
    }

    public void setMaxHoursPerEmployee(int maxHoursPerEmployee) {
        this.maxHoursPerEmployee = maxHoursPerEmployee;
    }

    public int getMaxShiftsPerEmployee() {
        return maxShiftsPerEmployee;
    }

    public void setMaxShiftsPerEmployee(int maxShiftsPerEmployee) {
        this.maxShiftsPerEmployee = maxShiftsPerEmployee;
    }

    public int getMaxHoursPerShift() {
        return maxHoursPerShift;
    }

    public void setMaxHoursPerShift(int maxHoursPerShift) {
        this.maxHoursPerShift = maxHoursPerShift;
    }

}
