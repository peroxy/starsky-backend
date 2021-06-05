package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.user.User;

import java.util.List;

public interface ScheduleSolveService {
    List<EmployeeAssignment> solveSchedule(long scheduleId, User user) throws ForbiddenException;
}
