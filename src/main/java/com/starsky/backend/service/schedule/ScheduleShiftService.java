package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;

import java.util.List;

public interface ScheduleShiftService {
    List<ScheduleShift> getScheduleShifts(long scheduleId, User manager) throws ForbiddenException;
}

