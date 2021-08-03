package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.exception.ScheduleUnsolvableException;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.team.TeamMember;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.service.schedule.ScheduleService;
import com.starsky.backend.service.team.TeamService;
import org.optaplanner.core.api.solver.SolverManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ScheduleSolveServiceImpl implements ScheduleSolveService {

    private final ScheduleService scheduleService;
    private final SolverManager<SolvedSchedule, UUID> solverManager;
    private final TeamService teamService;

    private final Logger logger = LoggerFactory.getLogger(ScheduleSolveServiceImpl.class);

    @Autowired
    public ScheduleSolveServiceImpl(ScheduleService scheduleService, SolverManager<SolvedSchedule, UUID> solverManager, TeamService teamService) {
        this.scheduleService = scheduleService;
        this.solverManager = solverManager;
        this.teamService = teamService;
    }

    @Override
    public List<EmployeeAssignment> solveSchedule(long scheduleId, User user) throws ForbiddenException, ResourceNotFoundException, ScheduleUnsolvableException {
        var schedule = scheduleService.getSchedule(scheduleId, user);
        var shifts = schedule.getShifts();

        if (shifts.size() == 0) {
            logger.warn("Schedule cannot be solved - it does not have any shifts assigned to it!");
            throw new ScheduleUnsolvableException("Schedule cannot be solved - it does not have any shifts assigned to it!");
        }

        var members = teamService.getTeamMembers(schedule.getTeam().getId(), user);
        var availableEmployeeIds = new HashSet<Long>();
        shifts.forEach(
                scheduleShift -> scheduleShift.getEmployeeAvailabilities().forEach(
                        availability -> availableEmployeeIds.add(availability.getEmployee().getId())));

        if (availableEmployeeIds.size() == 0) {
            logger.warn("Schedule cannot be solved - no available employees!");
            throw new ScheduleUnsolvableException("Schedule cannot be solved - no available employees!");
        }

        var employees = members.stream().filter(member -> availableEmployeeIds.contains(member.getMember().getId())).map(TeamMember::getMember).collect(Collectors.toList());
        var employeeAssignments = getEmployeeAssignments(shifts);

        // shuffle to avoid bias for the order of employees..
        Collections.shuffle(shifts);
        Collections.shuffle(employees);

        var job = solverManager.solve(UUID.randomUUID(), new SolvedSchedule(scheduleId, shifts, employees, employeeAssignments));

        SolvedSchedule solution;
        try {
            solution = job.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("solving failed", e);
            throw new ScheduleUnsolvableException("Solving failed due to %s.".formatted(e.getMessage()));
        }

        return solution.getEmployeeAssignments();
    }

    private List<EmployeeAssignment> getEmployeeAssignments(List<ScheduleShift> shifts) {
        var assignments = new ArrayList<EmployeeAssignment>();
        for (var shift : shifts) {
            for (int i = 0; i < shift.getNumberOfRequiredEmployees(); i++) {
                assignments.add(new EmployeeAssignment(null, shift, shift.getShiftStart(), shift.getShiftEnd()));
            }

        }
        return assignments;
    }
}

