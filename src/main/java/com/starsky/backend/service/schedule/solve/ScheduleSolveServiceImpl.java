package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.api.exception.ForbiddenException;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
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
    public List<EmployeeAssignment> solveSchedule(long scheduleId, User user) throws ForbiddenException, ResourceNotFoundException {
        var schedule = scheduleService.getSchedule(scheduleId, user);
        var shifts = schedule.getShifts();

        var members = teamService.getTeamMembers(schedule.getTeam().getId(), user);

        var availableEmployeeIds = new HashSet<Long>();
        shifts.forEach(
                scheduleShift -> scheduleShift.getEmployeeAvailabilities().forEach(
                        availability -> availableEmployeeIds.add(availability.getEmployee().getId())));

        var employees = members.stream().filter(member -> availableEmployeeIds.contains(member.getId())).map(TeamMember::getMember).collect(Collectors.toList());

        var employeeAssignments = getEmployeeAssignments(shifts);

        var job = solverManager.solve(UUID.randomUUID(), new SolvedSchedule(scheduleId, shifts, employees, employeeAssignments));

        SolvedSchedule solution;
        try {
            solution = job.getFinalBestSolution();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("solving failed", e);
            throw new IllegalStateException("Solving failed.", e);
            //TODO: throw something else and handle with rest controller?
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

