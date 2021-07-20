package com.starsky.backend.service.schedule.availability;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.availability.CreateEmployeeAvailabilitiesRequest;
import com.starsky.backend.api.schedule.availability.CreateEmployeeAvailabilityRequest;
import com.starsky.backend.api.schedule.availability.UpdateEmployeeAvailabilityRequest;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.EmployeeAvailabilityRepository;
import com.starsky.backend.service.schedule.DateRangeValidator;
import com.starsky.backend.service.schedule.shift.ScheduleShiftService;
import com.starsky.backend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeAvailabilityServiceImpl implements EmployeeAvailabilityService {

    private final DateRangeValidator dateRangeValidator;
    private final ScheduleShiftService scheduleShiftService;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(EmployeeAvailabilityServiceImpl.class);

    public EmployeeAvailabilityServiceImpl(DateRangeValidator dateRangeValidator, ScheduleShiftService scheduleShiftService,
                                           EmployeeAvailabilityRepository employeeAvailabilityRepository, UserService userService) {
        this.dateRangeValidator = dateRangeValidator;
        this.scheduleShiftService = scheduleShiftService;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.userService = userService;
    }

    @Override
    public List<EmployeeAvailability> getEmployeeAvailabilities(long shiftId, User user) throws ForbiddenException {
        var shift = scheduleShiftService.getScheduleShift(shiftId, user);
        return shift.getEmployeeAvailabilities();
    }

    @Override
    public EmployeeAvailability getEmployeeAvailability(long availabilityId, User user) throws ResourceNotFoundException, ForbiddenException {
        var manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }

        User finalManager = manager; //lmao java is great
        var availability = employeeAvailabilityRepository.getEmployeeAvailabilityByIdAndShiftScheduleTeamOwner(availabilityId, manager)
                .orElseThrow(() -> getResourceNotFoundException(availabilityId, finalManager));

        //this should throw forbidden if the user does not have permission for this resource.. good enough
        scheduleShiftService.getScheduleShift(availability.getShift().getId(), user);

        return availability;
    }

    @Override
    public EmployeeAvailability createEmployeeAvailability(long shiftId, CreateEmployeeAvailabilityRequest request, User manager)
            throws ResourceNotFoundException, DateRangeException, ForbiddenException {
        dateRangeValidator.validateDateInterval(request.getAvailabilityStart(), request.getAvailabilityEnd());
        var shift = scheduleShiftService.getScheduleShift(shiftId, manager);
        var employee = userService.getEmployeeById(request.getEmployeeId(), manager);
        var availability = new EmployeeAvailability(employee, shift, request.getAvailabilityStart(), request.getAvailabilityEnd(), request.getMaxHoursPerShift());

        checkIfDateIntervalExistsOrOverlaps(availability);

        availability = employeeAvailabilityRepository.save(availability);
        return availability;
    }

    private void checkIfDateIntervalExistsOrOverlaps(EmployeeAvailability availability) throws DateRangeException {
        List<EmployeeAvailability> existing = employeeAvailabilityRepository.findAllByEmployeeIdAndShiftIdAndAvailabilityBetween(
                availability.getEmployee().getId(),
                availability.getShift().getId(),
                availability.getAvailabilityStart(),
                availability.getAvailabilityEnd());

        for (var existingAvailability : existing) {
            if (!existingAvailability.getId().equals(availability.getId())) {
                throw new DateRangeException(
                        "Employee (id=%d) availability (id=%d) date range (from %s to %s) already exists or overlaps with another availability for this shift (id=%d)"
                                .formatted(availability.getEmployee().getId(),
                                        existingAvailability.getId(),
                                        existingAvailability.getAvailabilityStart(),
                                        existingAvailability.getAvailabilityEnd(),
                                        availability.getShift().getId()));
            }
        }
    }

    @Override
    public void deleteEmployeeAvailability(long availabilityId, User manager) throws ResourceNotFoundException {
        employeeAvailabilityRepository.getEmployeeAvailabilityByIdAndShiftScheduleTeamOwner(availabilityId, manager)
                .ifPresentOrElse(employeeAvailabilityRepository::delete, () -> {
                    throw getResourceNotFoundException(availabilityId, manager);
                });
    }

    @Override
    public EmployeeAvailability updateEmployeeAvailability(long availabilityId, UpdateEmployeeAvailabilityRequest request, User manager) throws ResourceNotFoundException, DateRangeException {
        var availability = employeeAvailabilityRepository.getEmployeeAvailabilityByIdAndShiftScheduleTeamOwner(availabilityId, manager)
                .orElseThrow(() -> getResourceNotFoundException(availabilityId, manager));

        if (request.getAvailabilityStart().isPresent()) {
            availability.setAvailabilityStart(request.getAvailabilityStart().get());
        }
        if (request.getAvailabilityEnd().isPresent()) {
            availability.setAvailabilityEnd(request.getAvailabilityEnd().get());
        }
        if (request.getMaxHoursPerShift().isPresent()) {
            availability.setMaxHoursPerShift(request.getMaxHoursPerShift().get());
        }

        dateRangeValidator.validateDateInterval(availability.getAvailabilityStart(), availability.getAvailabilityEnd());
        checkIfDateIntervalExistsOrOverlaps(availability);

        return employeeAvailabilityRepository.save(availability);
    }

    @Override
    @Transactional
    public void putAll(List<CreateEmployeeAvailabilitiesRequest> availabilities, User manager) throws DateRangeException {

        var shiftIds = availabilities.stream().map(CreateEmployeeAvailabilitiesRequest::getShiftId).collect(Collectors.toList());
        // validate
        // check if all shift ids exist, if all employee ids exist, if date range is correct,
        if (!scheduleShiftService.shiftsExist(shiftIds, manager)) {
            var message = "Schedule shift ids (one or many) invalid - they do not exist for this manager.";
            this.logger.warn(message);
            throw new ResourceNotFoundException(message);
        }
        ;
        if (!userService.employeesExist(availabilities.stream().map(CreateEmployeeAvailabilitiesRequest::getEmployeeId).toArray(Long[]::new), manager)) {
            var message = "Employee ids (one or many) invalid - they do not exist for this manager.";
            this.logger.warn(message);
            throw new ResourceNotFoundException(message);
        }

        var employeeAvailabilities = new ArrayList<EmployeeAvailability>();
        for (var availability : availabilities) {
            dateRangeValidator.validateDateInterval(availability.getAvailabilityStart(), availability.getAvailabilityEnd());
            var user = new User();
            user.setId(availability.getEmployeeId());
            var shift = new ScheduleShift();
            shift.setId(availability.getShiftId());
            employeeAvailabilities.add(
                    new EmployeeAvailability(user, shift, availability.getAvailabilityStart(), availability.getAvailabilityEnd(), availability.getMaxHoursPerShift()));
        }

//        employeeAvailabilityRepository.deleteAllByShiftIdInAndShiftScheduleTeamOwner(shiftIds, manager);
        employeeAvailabilityRepository.saveAll(employeeAvailabilities);
    }

    private ResourceNotFoundException getResourceNotFoundException(long availabilityId, User manager) {
        var message = "Employee availability (id=%d, owner=%d) does not exist.".formatted(availabilityId, manager.getId());
        this.logger.warn(message);
        return new ResourceNotFoundException(message);
    }
}
