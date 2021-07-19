package com.starsky.backend.service.schedule.shift;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.shift.CreateScheduleShiftRequest;
import com.starsky.backend.api.schedule.shift.UpdateScheduleShiftRequest;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.ScheduleShiftRepository;
import com.starsky.backend.service.schedule.DateRangeValidator;
import com.starsky.backend.service.schedule.ScheduleService;
import com.starsky.backend.service.team.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ScheduleShiftServiceImpl implements ScheduleShiftService {

    private final ScheduleShiftRepository scheduleShiftRepository;
    private final ScheduleService scheduleService;
    private final TeamService teamService;
    private final DateRangeValidator dateRangeValidator;
    private final Logger logger = LoggerFactory.getLogger(ScheduleShiftServiceImpl.class);

    public ScheduleShiftServiceImpl(ScheduleShiftRepository scheduleShiftRepository, ScheduleService scheduleService, TeamService teamService, DateRangeValidator dateRangeValidator) {
        this.scheduleShiftRepository = scheduleShiftRepository;
        this.scheduleService = scheduleService;
        this.teamService = teamService;
        this.dateRangeValidator = dateRangeValidator;
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

    @Override
    public ScheduleShift getScheduleShift(long shiftId, User user) throws ForbiddenException, ResourceNotFoundException {
        User manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }

        User finalManager = manager; //lol java
        var scheduleShift = scheduleShiftRepository.getByIdAndScheduleTeamOwner(shiftId, manager).orElseThrow(() -> {
            return getShiftDoesNotExistException(shiftId, finalManager);
        });

        if (user.getRole() == Role.EMPLOYEE && teamService.getTeams(user).stream().noneMatch(team -> team.getId() == scheduleShift.getSchedule().getTeam().getId())) {
            var message =
                    "Authenticated user (id=%d) does not have necessary permissions to access this schedule's shifts - does not belong to schedule's team."
                            .formatted(user.getId());
            this.logger.warn(message);
            throw new ForbiddenException(message);
        }

        return scheduleShift;
    }

    @Override
    public ScheduleShift createScheduleShift(long scheduleId, CreateScheduleShiftRequest shiftRequest, User manager) throws DateRangeException, ForbiddenException {
        dateRangeValidator.validateDateInterval(shiftRequest.getShiftStart(), shiftRequest.getShiftEnd());
        var schedule = scheduleService.getSchedule(scheduleId, manager); // will throw resource not found if it doesn't exist
        var scheduleShift = new ScheduleShift(shiftRequest.getShiftStart(), shiftRequest.getShiftEnd(), schedule, shiftRequest.getNumberOfRequiredEmployees());
        checkIfDateIntervalExistsOrOverlaps(scheduleShift);
        scheduleShift = scheduleShiftRepository.save(scheduleShift);
        return scheduleShift;
    }

    @Override
    public void deleteScheduleShift(long shiftId, User manager) throws ResourceNotFoundException {
        scheduleShiftRepository.getByIdAndScheduleTeamOwner(shiftId, manager)
                .ifPresentOrElse(scheduleShiftRepository::delete, () -> {
                    throw getShiftDoesNotExistException(shiftId, manager);
                });
    }

    @Override
    public ScheduleShift updateScheduleShift(long shiftId, UpdateScheduleShiftRequest request, User manager) throws ResourceNotFoundException, DateRangeException {
        var scheduleShift = scheduleShiftRepository.getByIdAndScheduleTeamOwner(shiftId, manager).orElseThrow(() -> {
            throw getShiftDoesNotExistException(shiftId, manager);
        });

        if (request.getShiftStart().isPresent()) {
            scheduleShift.setShiftStart(request.getShiftStart().get());
        }
        if (request.getShiftEnd().isPresent()) {
            scheduleShift.setShiftEnd(request.getShiftEnd().get());
        }
        if (request.getNumberOfRequiredEmployees().isPresent()) {
            scheduleShift.setNumberOfRequiredEmployees(request.getNumberOfRequiredEmployees().get());
        }
        dateRangeValidator.validateDateInterval(scheduleShift.getShiftStart(), scheduleShift.getShiftEnd());
        checkIfDateIntervalExistsOrOverlaps(scheduleShift);
        return scheduleShiftRepository.save(scheduleShift);
    }

    @Override
    public boolean shiftsExist(Collection<Long> shiftIds, User owner) {
        return scheduleShiftRepository.existsByIdInAndScheduleTeamOwner(shiftIds, owner);
    }

    @Override
    @Transactional
    public List<ScheduleShift> putAll(List<CreateScheduleShiftRequest> requests, User owner, long scheduleId) throws DateRangeException, ResourceNotFoundException, ForbiddenException {
        var schedule = scheduleService.getSchedule(scheduleId, owner);

        var shifts = new ArrayList<ScheduleShift>();
        for (var shift : requests) {
            dateRangeValidator.validateDateInterval(shift.getShiftStart(), shift.getShiftEnd());
            shifts.add(new ScheduleShift(shift.getShiftStart(), shift.getShiftEnd(), schedule, shift.getNumberOfRequiredEmployees()));
        }

        scheduleShiftRepository.deleteByScheduleIdAndScheduleTeamOwner(scheduleId, owner);
        return scheduleShiftRepository.saveAll(shifts);
    }

    private ResourceNotFoundException getShiftDoesNotExistException(long shiftId, User manager) {
        var message = "Schedule shift (id=%d, owner=%d) does not exist.".formatted(shiftId, manager.getId());
        this.logger.warn(message);
        return new ResourceNotFoundException(message);
    }

    private void checkIfDateIntervalExistsOrOverlaps(ScheduleShift shift) throws DateRangeException {
        var existing = scheduleShiftRepository.findAllByScheduleIdAndShiftBetween(shift.getSchedule().getId(), shift.getShiftStart(), shift.getShiftEnd());
        for (var existingShift : existing) {
            if (!existingShift.getId().equals(shift.getId())) {
                throw new DateRangeException(
                        "Schedule shift (id=%d) date range (from %s to %s) already exists or overlaps with another shift for this schedule (id=%d)"
                                .formatted(existingShift.getId(),
                                        existingShift.getShiftStart(),
                                        existingShift.getShiftEnd(),
                                        existingShift.getSchedule().getId()));
            }
        }
    }
}
