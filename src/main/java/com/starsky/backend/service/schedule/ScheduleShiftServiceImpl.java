package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.ScheduleShiftRepository;
import com.starsky.backend.service.team.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleShiftServiceImpl implements ScheduleShiftService {

    private final ScheduleShiftRepository scheduleShiftRepository;
    private final ScheduleService scheduleService;
    private final TeamService teamService;
    private final Logger logger = LoggerFactory.getLogger(ScheduleShiftServiceImpl.class);

    public ScheduleShiftServiceImpl(ScheduleShiftRepository scheduleShiftRepository, ScheduleService scheduleService, TeamService teamService) {
        this.scheduleShiftRepository = scheduleShiftRepository;
        this.scheduleService = scheduleService;
        this.teamService = teamService;
    }

    @Override
    public List<ScheduleShift> getScheduleShifts(long scheduleId, User user) throws ForbiddenException {
        User manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }
        var schedule = scheduleService.getSchedule(scheduleId, user); //throws resource not found if schedule does not exist

        if (user.getRole() == Role.EMPLOYEE && teamService.getTeams(user).stream().noneMatch(team -> team.getId() == schedule.getTeam().getId())) {
            var message =
                    "Authenticated user (id=%d) does not have necessary permissions to access this schedule's shifts - does not belong to schedule's team."
                            .formatted(user.getId());
            this.logger.warn(message);
            throw new ForbiddenException(message);
        }

        return scheduleShiftRepository.getAllByScheduleAndScheduleTeamOwner(schedule, manager);

    }
}
