package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.CreateScheduleRequest;
import com.starsky.backend.api.schedule.UpdateScheduleRequest;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface ScheduleService {
    List<Schedule> getSchedules(User user);

    Schedule getSchedule(long scheduleId, User user) throws ResourceNotFoundException, ForbiddenException;

    List<Schedule> getSchedulesByTeam(User user, long teamId);

    Schedule createSchedule(CreateScheduleRequest request, long teamId, User user) throws DateRangeException, ResourceNotFoundException;

    Schedule updateSchedule(UpdateScheduleRequest request, long scheduleId, User owner) throws DateRangeException, ResourceNotFoundException, ForbiddenException;

    void deleteSchedule(long scheduleId, User owner) throws ResourceNotFoundException, ForbiddenException;

    void notifyEmployees(long scheduleId, User owner) throws ResourceNotFoundException, ForbiddenException;
}
