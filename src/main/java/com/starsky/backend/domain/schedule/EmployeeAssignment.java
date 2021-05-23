package com.starsky.backend.domain.schedule;

import com.starsky.backend.api.schedule.assignment.EmployeeAssignmentResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
public class EmployeeAssignment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employee-assignment-id-generator")
    @SequenceGenerator(name = "employee-assignment-id-generator", sequenceName = "employee_assignment_sequence", allocationSize = 1)
    private Long id;
    @OneToOne
    @NotNull
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
    }

    public EmployeeAssignment() {
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

}
