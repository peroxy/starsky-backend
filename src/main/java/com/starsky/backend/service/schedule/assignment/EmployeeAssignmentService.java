package com.starsky.backend.service.schedule.assignment;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.assignment.CreateEmployeeAssignmentRequest;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.user.User;

import java.util.List;

public interface EmployeeAssignmentService {
    List<EmployeeAssignment> getAll(long scheduleId, User user) throws ForbiddenException;

    void putAll(List<CreateEmployeeAssignmentRequest> requests, long scheduleId, User owner) throws ForbiddenException, DateRangeException;
}
