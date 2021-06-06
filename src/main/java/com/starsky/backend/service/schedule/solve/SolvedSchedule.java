package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;
import java.util.stream.Collectors;

@PlanningSolution
public class SolvedSchedule {
    private long scheduleId;

    @ProblemFactCollectionProperty
    private List<ScheduleShift> shifts;

    @ProblemFactCollectionProperty
    private List<EmployeeAvailability> availabilities;

    @ValueRangeProvider(id = "employeeRange")
    @ProblemFactCollectionProperty
    private List<User> employees;

    @PlanningEntityCollectionProperty
    private List<EmployeeAssignment> employeeAssignments;

    @PlanningScore
    private HardSoftScore score;

    public SolvedSchedule(long scheduleId, List<ScheduleShift> shifts, List<User> employees, List<EmployeeAssignment> employeeAssignments) {
        this.scheduleId = scheduleId;
        this.shifts = shifts;
        this.employees = employees;
        this.employeeAssignments = employeeAssignments;
        this.availabilities = shifts.stream().flatMap(scheduleShift -> scheduleShift.getEmployeeAvailabilities().stream()).collect(Collectors.toList());
    }

    public SolvedSchedule() {

    }

    public long getScheduleId() {
        return scheduleId;
    }

    public List<ScheduleShift> getShifts() {
        return shifts;
    }

    public List<User> getEmployees() {
        return employees;
    }

    public List<EmployeeAssignment> getEmployeeAssignments() {
        return employeeAssignments;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public List<EmployeeAvailability> getAvailabilities() {
        return availabilities;
    }
}
