package com.starsky.backend.service.schedule;

import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleShiftServiceImpl implements ScheduleShiftService {

    @Override
    public List<ScheduleShift> getScheduleShifts(long scheduleId, User manager) {
        throw new NotImplementedException();
    }
}
