package com.starsky.backend.domain.schedule;

import com.starsky.backend.api.schedule.ScheduleResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.team.Team;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

@Entity
public class Schedule extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule-id-generator")
    @SequenceGenerator(name = "schedule-id-generator", sequenceName = "schedule_sequence")
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Instant scheduleStart;
    @NotNull
    private Instant scheduleEnd;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private Team team;
    @NotNull
    @Min(0)
    private int maxHoursPerEmployee;
    @NotNull
    @Min(0)
    private int maxShiftsPerEmployee;
    @NotNull
    @Min(0)
    private int maxHoursPerShift;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleShift> shifts;


    public Schedule(@NotNull String name,
                    @NotNull Instant scheduleStart,
                    @NotNull Instant scheduleEnd,
                    @NotNull Team team,
                    @NotNull int maxHoursPerEmployee,
                    @NotNull int maxShiftsPerEmployee,
                    @NotNull int maxHoursPerShift) {
        this.name = name;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
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

    public Instant getScheduleStart() {
        return scheduleStart;
    }

    public void setScheduleStart(Instant start) {
        this.scheduleStart = start;
    }

    public Instant getScheduleEnd() {
        return scheduleEnd;
    }

    public void setScheduleEnd(Instant end) {
        this.scheduleEnd = end;
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

    public ScheduleResponse toResponse() {
        return new ScheduleResponse(id, name, scheduleStart, scheduleEnd, team.getId(), maxHoursPerEmployee, maxShiftsPerEmployee, maxHoursPerShift);
    }

    public List<ScheduleShift> getShifts() {
        return shifts;
    }
}
