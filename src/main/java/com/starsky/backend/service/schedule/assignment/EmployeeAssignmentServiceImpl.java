package com.starsky.backend.service.schedule.assignment;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.assignment.CreateEmployeeAssignmentRequest;
import com.starsky.backend.api.schedule.assignment.PutEmployeeAssignmentRequest;
import com.starsky.backend.api.schedule.assignment.UpdateEmployeeAssignmentRequest;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.EmployeeAssignmentRepository;
import com.starsky.backend.service.schedule.DateRangeValidator;
import com.starsky.backend.service.schedule.ScheduleService;
import com.starsky.backend.service.schedule.shift.ScheduleShiftService;
import com.starsky.backend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeAssignmentServiceImpl implements EmployeeAssignmentService {

    private final ScheduleService scheduleService;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final DateRangeValidator dateRangeValidator;
    private final ScheduleShiftService scheduleShiftService;
    private final UserService userService;

    private final Logger logger = LoggerFactory.getLogger(EmployeeAssignmentServiceImpl.class);

    @Autowired
    public EmployeeAssignmentServiceImpl(ScheduleService scheduleService, EmployeeAssignmentRepository employeeAssignmentRepository, DateRangeValidator dateRangeValidator, ScheduleShiftService scheduleShiftService, UserService userService) {
        this.scheduleService = scheduleService;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.dateRangeValidator = dateRangeValidator;
        this.scheduleShiftService = scheduleShiftService;
        this.userService = userService;
    }

    @Override
    public List<EmployeeAssignment> getAll(long scheduleId, User user) throws ForbiddenException {
        var schedule = scheduleService.getSchedule(scheduleId, user);
        return employeeAssignmentRepository.getAllByShiftSchedule(schedule);
    }

    @Override
    public EmployeeAssignment update(long assignmentId, long scheduleId, User user, UpdateEmployeeAssignmentRequest request) throws ForbiddenException, DateRangeException {
        var schedule = scheduleService.getSchedule(scheduleId, user); // this will throw if user does not have permission or if schedule does not exist

        var assignment = employeeAssignmentRepository.findByIdAndShiftSchedule(assignmentId, schedule).orElseThrow(() -> {
            var error = "Assignment (id=%d) does not exist for schedule (id=%d).".formatted(assignmentId, scheduleId);
            return logAndGetResourceNotFound(error);
        });

        if (request.getAssignmentStart().isPresent()) {
            assignment.setAssignmentStart(request.getAssignmentStart().get());
        }

        if (request.getAssignmentEnd().isPresent()) {
            assignment.setAssignmentEnd(request.getAssignmentEnd().get());
        }

        validateAssignmentDateRange(assignment.getAssignmentStart(), assignment.getAssignmentEnd(), assignment.getShift(), assignment.getEmployee().getId());

        return employeeAssignmentRepository.save(assignment);
    }

    @Override
    public void delete(long assignmentId, long scheduleId, User user) throws ForbiddenException, ResourceNotFoundException {
        var schedule = scheduleService.getSchedule(scheduleId, user); // this will throw if user does not have permission or if schedule does not exist

        var assignment = employeeAssignmentRepository.findByIdAndShiftSchedule(assignmentId, schedule).orElseThrow(() -> {
            var error = "Assignment (id=%d) does not exist for schedule (id=%d).".formatted(assignmentId, scheduleId);
            return logAndGetResourceNotFound(error);
        });

        employeeAssignmentRepository.delete(assignment);
    }

    @Override
    public EmployeeAssignment create(long scheduleId, long shiftId, long employeeId, User owner, CreateEmployeeAssignmentRequest request) throws ForbiddenException, ResourceNotFoundException, DateRangeException {
        var shift = scheduleShiftService.getScheduleShift(shiftId, owner);
        var employee = userService.getEmployeeById(employeeId, owner);

        validateAssignmentDateRange(request.getAssignmentStart(), request.getAssignmentEnd(), shift, employeeId);

        var assignment = new EmployeeAssignment(employee, shift, request.getAssignmentStart(), request.getAssignmentEnd());
        return employeeAssignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void putAll(List<PutEmployeeAssignmentRequest> requests, long scheduleId, User owner) throws ForbiddenException, DateRangeException {
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

            validateAssignmentDateRange(request.getAssignmentStart(), request.getAssignmentEnd(), shift, request.getEmployeeId());

            assignments.add(new EmployeeAssignment(employee.getMember(), shift, request.getAssignmentStart(), request.getAssignmentEnd()));
        }


        // we were checking if any of the timestamps were overlapped in the request, but this should not be needed,
        // let the user shoot in the foot if they want to schedule someone like this
//        var grouped = requests.stream().collect(Collectors.groupingBy(PutEmployeeAssignmentRequest::getEmployeeId));
//        for (var group : grouped.entrySet()) {
//            anyTimestampsOverlap(group.getValue().toArray(PutEmployeeAssignmentRequest[]::new));
//        }

        // 2. validation OK, delete old assignments as a transaction, should roll back if anything goes wrong when deleting or saving
        employeeAssignmentRepository.deleteAllByShiftSchedule(schedule);

        // 3. save all assignments
        employeeAssignmentRepository.saveAll(assignments);
    }

    private void validateAssignmentDateRange(Instant start, Instant end, ScheduleShift shift, long employeeId) throws DateRangeException {
        dateRangeValidator.validateDateInterval(start, end);

        if (start.isBefore(shift.getShiftStart()) || start.isAfter(shift.getShiftEnd()) || end.isBefore(shift.getShiftStart()) || end.isAfter(shift.getShiftEnd())) {
            var error = "Employee assignment (employee id=%d) date range does not overlap with schedule shift date range (assignment (from %s to %s), shift (from %s to %s))."
                    .formatted(employeeId, start, end, shift.getShiftStart(), shift.getShiftEnd());
            logger.warn(error);
            throw new DateRangeException(error);
        }
    }

    private void anyTimestampsOverlap(PutEmployeeAssignmentRequest[] requests) throws DateRangeException {
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
