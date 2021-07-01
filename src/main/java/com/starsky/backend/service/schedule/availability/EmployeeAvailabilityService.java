package com.starsky.backend.service.schedule.availability;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.availability.CreateEmployeeAvailabilitiesRequest;
import com.starsky.backend.api.schedule.availability.CreateEmployeeAvailabilityRequest;
import com.starsky.backend.api.schedule.availability.UpdateEmployeeAvailabilityRequest;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface EmployeeAvailabilityService {
    List<EmployeeAvailability> getEmployeeAvailabilities(long shiftId, User user) throws ForbiddenException;

    EmployeeAvailability getEmployeeAvailability(long availabilityId, User user) throws ResourceNotFoundException, ForbiddenException;

    EmployeeAvailability createEmployeeAvailability(long shiftId, CreateEmployeeAvailabilityRequest request, User manager) throws ResourceNotFoundException, DateRangeException, ForbiddenException;

    void deleteEmployeeAvailability(long availabilityId, User manager) throws ResourceNotFoundException;

    EmployeeAvailability updateEmployeeAvailability(long availabilityId, UpdateEmployeeAvailabilityRequest request, User manager) throws ResourceNotFoundException, DateRangeException;

    void putAll(List<CreateEmployeeAvailabilitiesRequest> availabilities, User manager) throws DateRangeException;
}
