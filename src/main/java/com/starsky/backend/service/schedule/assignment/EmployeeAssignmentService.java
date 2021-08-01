package com.starsky.backend.service.schedule.assignment;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.assignment.CreateEmployeeAssignmentRequest;
import com.starsky.backend.api.schedule.assignment.PutEmployeeAssignmentRequest;
import com.starsky.backend.api.schedule.assignment.UpdateEmployeeAssignmentRequest;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface EmployeeAssignmentService {
    List<EmployeeAssignment> getAll(long scheduleId, User user) throws ForbiddenException, ResourceNotFoundException;

    EmployeeAssignment update(long assignmentId, long scheduleId, User user, UpdateEmployeeAssignmentRequest request) throws ForbiddenException, DateRangeException, ResourceNotFoundException;

    void delete(long assignmentId, long scheduleId, User user) throws ForbiddenException, ResourceNotFoundException;

    EmployeeAssignment create(long scheduleId, long shiftId, long employeeId, User owner, CreateEmployeeAssignmentRequest request) throws ForbiddenException, ResourceNotFoundException, DateRangeException;

    void putAll(List<PutEmployeeAssignmentRequest> requests, long scheduleId, User owner) throws ForbiddenException, DateRangeException, ResourceNotFoundException;
}
