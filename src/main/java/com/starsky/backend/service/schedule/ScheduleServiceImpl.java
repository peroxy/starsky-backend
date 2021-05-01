package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.CreateScheduleRequest;
import com.starsky.backend.api.schedule.UpdateScheduleRequest;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.ScheduleRepository;
import com.starsky.backend.service.team.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        var manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }

        var schedules = scheduleRepository.findAllByTeamOwner(manager);

        if (user.getRole() == Role.EMPLOYEE) {
            var teams = teamService.getTeams(user);
            schedules.removeIf(schedule -> teams.stream().noneMatch(team -> team.getId() == schedule.getTeam().getId()));
        }

        return schedules;
    }

    @Override
    public Schedule getSchedule(long scheduleId, User user) throws ResourceNotFoundException, ForbiddenException {
        var manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }
        var schedule = scheduleRepository.findByIdAndTeamOwner(scheduleId, manager).orElseThrow(() -> {
            var error = "Schedule (id=%d) does not exist.".formatted(scheduleId);
            this.logger.warn(error);
            throw new ResourceNotFoundException(error);
        });

        if (user.getRole() == Role.EMPLOYEE) {
            var teams = teamService.getTeams(user);
            if (teams.stream().noneMatch(team -> team.getId() == schedule.getTeam().getId())) {
                var message =
                        "Authenticated user (id=%d) does not have necessary permissions to access this schedule - does not belong to schedule's team."
                                .formatted(user.getId());
                this.logger.warn(message);
                throw new ForbiddenException(message);
            }
        }

        return schedule;
    }

    @Override
    public List<Schedule> getSchedulesByTeam(User user, long teamId) {
        return scheduleRepository.findAllByTeamOwnerAndTeamId(user, teamId);
    }

    @Override
    public Schedule createSchedule(CreateScheduleRequest request, long teamId, User owner) throws DateRangeException, ResourceNotFoundException {
        validateDateInterval(request.getScheduleStart(), request.getScheduleEnd());

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

    private void validateDateInterval(Instant start, Instant end) throws DateRangeException {
        String error = null;
        if (start.isAfter(end)) {
            error = "Schedule start timestamp (%s) occurs after schedule end timestamp (%s). Start date has to occur before end date."
                    .formatted(start, end);
        } else if (start.equals(end)) {
            error = "Schedule start timestamp (%s) equals after schedule end timestamp (%s). Start and end date cannot be equal."
                    .formatted(start, end);
        }

        if (error != null) {
            this.logger.warn(error);
            throw new DateRangeException(error);
        }
    }

    @Override
    public Schedule updateSchedule(UpdateScheduleRequest request, long scheduleId, User owner) throws DateRangeException, ResourceNotFoundException, ForbiddenException {
        var schedule = getSchedule(scheduleId, owner);
        if (request.getScheduleName().isPresent()) {
            schedule.setName(request.getScheduleName().get());
        }
        if (request.getScheduleStart().isPresent()) {
            schedule.setScheduleStart(request.getScheduleStart().get());
        }
        if (request.getScheduleEnd().isPresent()) {
            schedule.setScheduleEnd(request.getScheduleEnd().get());
        }
        if (request.getMaxHoursPerEmployee().isPresent()) {
            schedule.setMaxHoursPerEmployee(request.getMaxHoursPerEmployee().get());
        }
        if (request.getMaxHoursPerShift().isPresent()) {
            schedule.setMaxHoursPerShift(request.getMaxHoursPerShift().get());
        }
        if (request.getMaxShiftsPerEmployee().isPresent()) {
            schedule.setMaxShiftsPerEmployee(request.getMaxShiftsPerEmployee().get());
        }
        validateDateInterval(schedule.getScheduleStart(), schedule.getScheduleEnd());
        return scheduleRepository.save(schedule);
    }


    @Override
    public void deleteSchedule(long scheduleId, User owner) throws ResourceNotFoundException, ForbiddenException {
        getSchedule(scheduleId, owner); // will throw resource not found if not found
        scheduleRepository.deleteById(scheduleId);
    }

}
