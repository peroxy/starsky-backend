package com.starsky.backend.service.schedule.shift;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.shift.CreateScheduleShiftRequest;
import com.starsky.backend.api.schedule.shift.UpdateScheduleShiftRequest;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.Collection;
import java.util.List;

public interface ScheduleShiftService {
    List<ScheduleShift> getScheduleShifts(long scheduleId, User user) throws ForbiddenException;

    ScheduleShift getScheduleShift(long shiftId, User user) throws ForbiddenException, ResourceNotFoundException;

    ScheduleShift createScheduleShift(long scheduleId, CreateScheduleShiftRequest shiftRequest, User manager) throws DateRangeException, ForbiddenException;

    void deleteScheduleShift(long shiftId, User manager) throws ResourceNotFoundException;

    ScheduleShift updateScheduleShift(long shiftId, UpdateScheduleShiftRequest request, User manager) throws DateRangeException;

    boolean shiftsExist(Collection<Long> shiftIds, User owner);

    List<ScheduleShift> putAll(List<CreateScheduleShiftRequest> shifts, User owner, long scheduleId) throws DateRangeException, ResourceNotFoundException, ForbiddenException;
}

