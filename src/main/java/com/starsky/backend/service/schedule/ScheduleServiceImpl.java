package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.schedule.CreateScheduleRequest;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.ScheduleRepository;
import com.starsky.backend.service.team.TeamService;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeamService teamService;
    private final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, TeamService teamService) {
        this.scheduleRepository = scheduleRepository;
        this.teamService = teamService;
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
    public Schedule createSchedule(CreateScheduleRequest request, long teamId, User owner) throws DateRangeException, ResourceNotFoundException {
        if (request.getScheduleStart().isAfter(request.getScheduleEnd())) {
            throw new DateRangeException("Schedule start timestamp (%s) occurs after schedule end timestamp (%s). Start date has to occur before end date."
                    .formatted(request.getScheduleStart(), request.getScheduleEnd()));
        }

        var team = teamService.getTeam(teamId, owner);

        var schedule = new Schedule(request.getScheduleName(),
                request.getScheduleStart(),
                request.getScheduleEnd(),
                team,
                request.getMaxHoursPerEmployee(),
                request.getMaxShiftsPerEmployee(),
                request.getMaxHoursPerShift());

        return scheduleRepository.save(schedule);
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
