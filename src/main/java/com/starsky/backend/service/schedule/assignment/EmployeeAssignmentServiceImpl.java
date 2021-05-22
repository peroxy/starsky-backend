package com.starsky.backend.service.schedule.assignment;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.assignment.CreateEmployeeAssignmentRequest;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.EmployeeAssignmentRepository;
import com.starsky.backend.service.schedule.DateRangeValidator;
import com.starsky.backend.service.schedule.ScheduleService;
import com.starsky.backend.service.team.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeAssignmentServiceImpl implements EmployeeAssignmentService {

    private final ScheduleService scheduleService;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final DateRangeValidator dateRangeValidator;

    private final Logger logger = LoggerFactory.getLogger(EmployeeAssignmentServiceImpl.class);

    @Autowired
    public EmployeeAssignmentServiceImpl(ScheduleService scheduleService, EmployeeAssignmentRepository employeeAssignmentRepository, TeamService teamService, DateRangeValidator dateRangeValidator) {
        this.scheduleService = scheduleService;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.dateRangeValidator = dateRangeValidator;
    }

    @Override
    public List<EmployeeAssignment> getAll(long scheduleId, User user) throws ForbiddenException {
        var schedule = scheduleService.getSchedule(scheduleId, user);
        return employeeAssignmentRepository.getAllByShiftSchedule(schedule);
    }

    @Override
    @Transactional
    public void putAll(List<CreateEmployeeAssignmentRequest> requests, long scheduleId, User owner) throws ForbiddenException, DateRangeException {
        // 1. validate schedule, shifts, user permissions, time intervals (they dont overlap) before saving anything
        var schedule = scheduleService.getSchedule(scheduleId, owner);
        var employees = schedule.getTeam().getTeamMembers();
        var shifts = schedule.getShifts();

        var assignments = new ArrayList<EmployeeAssignment>();

        for (var request : requests) {
            var employee = employees.stream().filter(teamMember -> teamMember.getMember().getId() == request.getEmployeeId()).findFirst().orElseThrow(() -> {
                var error = "Employee (user id=%d) does not exist.".formatted(request.getEmployeeId());
                return logAndGetResourceNotFound(error);
            });
            var shift = shifts.stream().filter(s -> s.getId() == request.getShiftId()).findFirst().orElseThrow(() -> {
                var error = "Shift (id=%d) does not exist.".formatted(request.getShiftId());
                return logAndGetResourceNotFound(error);
            });

            dateRangeValidator.validateDateInterval(request.getAssignmentStart(), request.getAssignmentEnd());

            assignments.add(new EmployeeAssignment(employee.getMember(), shift, request.getAssignmentStart(), request.getAssignmentEnd()));
        }

        var grouped = requests.stream().collect(Collectors.groupingBy(CreateEmployeeAssignmentRequest::getEmployeeId));
        for (var group : grouped.entrySet()) {
            anyTimestampsOverlap(group.getValue().toArray(CreateEmployeeAssignmentRequest[]::new));
        }

        // 2. validation OK, delete old assignments as a transaction, should rollback if anything goes wrong when deleting or saving
        employeeAssignmentRepository.deleteAllByShiftSchedule(schedule);

        // 3. save all assignments
        employeeAssignmentRepository.saveAll(assignments);
    }

    private void anyTimestampsOverlap(CreateEmployeeAssignmentRequest[] requests) throws DateRangeException {
        for (int i = 0; i < requests.length; i++) {
            var request = requests[i];
            for (int j = i + 1; j < requests.length; j++) {
                if ((request.getAssignmentStart().isBefore(requests[j].getAssignmentEnd()) || request.getAssignmentStart().equals(requests[j].getAssignmentEnd()))
                        && (requests[j].getAssignmentStart().isBefore(request.getAssignmentEnd()) || request.getAssignmentEnd().equals(requests[j].getAssignmentStart()))) {
                    throw new DateRangeException(
                            "Employee assignment (employee id=%d) date range overlaps with another (assignment 1 (from %s to %s) and assignment 2 (from %s to %s))."
                                    .formatted(request.getEmployeeId(), request.getAssignmentStart(), request.getAssignmentEnd(), requests[j].getAssignmentStart(), requests[j].getAssignmentEnd()));
                }
            }
        }
    }


    private ResourceNotFoundException logAndGetResourceNotFound(String error) {
        this.logger.warn(error);
        return new ResourceNotFoundException(error);
    }
}
