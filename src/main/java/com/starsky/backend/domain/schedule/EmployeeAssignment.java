package com.starsky.backend.domain.schedule;

import com.starsky.backend.api.schedule.assignment.EmployeeAssignmentResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@PlanningEntity
public class EmployeeAssignment extends BaseEntity {
    // planning ID and shift date are used for OptaPlanner and are transient (not an actual field in database)
    // entity ID isn't used since it's a generated value and we can't get it's value before persisting (at least not an easy way to do it or without hitting DB)
    @Transient
    @PlanningId
    private final UUID planningId = UUID.randomUUID();
    @Transient
    private ShiftDate shiftDate;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee-assignment-id-generator")
    @SequenceGenerator(name = "employee-assignment-id-generator", sequenceName = "employee_assignment_sequence")
    private Long id;
    @OneToOne
    @NotNull
    @PlanningVariable(valueRangeProviderRefs = {"employeeRange"})
    private User employee;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    private ScheduleShift shift;
    @NotNull
    private Instant assignmentStart;
    @NotNull
    private Instant assignmentEnd;

    public EmployeeAssignment(@NotNull User employee,
                              @NotNull ScheduleShift shift,
                              @NotNull Instant assignmentStart,
                              @NotNull Instant assignmentEnd) {
        this.employee = employee;
        this.shift = shift;
        this.assignmentStart = assignmentStart;
        this.assignmentEnd = assignmentEnd;
        this.shiftDate = new ShiftDate(assignmentStart, assignmentEnd);
    }

    public EmployeeAssignment() {
    }

    @PostLoad
    private void onLoad() {
        this.shiftDate = new ShiftDate(assignmentStart, assignmentEnd);
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

    public Instant getAssignmentStart() {
        return assignmentStart;
    }

    public void setAssignmentStart(Instant assignmentStart) {
        this.assignmentStart = assignmentStart;
    }

    public Instant getAssignmentEnd() {
        return assignmentEnd;
    }

    public void setAssignmentEnd(Instant assignmentEnd) {
        this.assignmentEnd = assignmentEnd;
    }

    public EmployeeAssignmentResponse toResponse() {
        return new EmployeeAssignmentResponse(id, assignmentStart, assignmentEnd, employee.getId(), shift.getId());
    }

    public ShiftDate getShiftDate() {
        return shiftDate;
    }

    public int getShiftDayNumber() {
        return assignmentStart.atOffset(ZoneOffset.UTC).getDayOfMonth();
    }

    public long getHourDuration() {
        return Duration.between(assignmentStart, assignmentEnd).toHours();
    }

    public void setShiftDate(ShiftDate shiftDate) {
        this.shiftDate = shiftDate;
    }
}
