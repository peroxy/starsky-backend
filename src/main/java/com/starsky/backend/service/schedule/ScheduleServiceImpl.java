package com.starsky.backend.service.schedule;

import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.ScheduleRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public List<Schedule> getSchedules(User user) {
        return scheduleRepository.findAllByTeamOwner(user);
    }

    @Override
    public Schedule getSchedule(long scheduleId, User user) throws ResourceNotFoundException {
        return scheduleRepository.findByIdAndTeamOwner(scheduleId, user).orElseThrow(() -> {
            var error = "Schedule (id=%d) does not exist.".formatted(scheduleId);
            this.logger.warn(error);
            throw new ResourceNotFoundException(error);
        });
    }

    @Override
    public List<Schedule> getSchedulesByTeam(User user, long teamId) {
        return scheduleRepository.findAllByTeamOwnerAndTeamId(user, teamId);
    }

    @Override
    public Schedule createSchedule(Schedule schedule) throws DataIntegrityViolationException {
        throw new NotImplementedException();
    }

    @Override
    public Schedule updateSchedule(Schedule schedule) {
        throw new NotImplementedException();
    }

    @Override
    public void deleteSchedule(long scheduleId) throws ResourceNotFoundException {
        throw new NotImplementedException();
    }
}
