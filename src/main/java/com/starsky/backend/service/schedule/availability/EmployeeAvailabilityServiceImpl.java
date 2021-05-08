package com.starsky.backend.service.schedule.availability;

import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.availability.CreateEmployeeAvailabilityRequest;
import com.starsky.backend.api.schedule.availability.UpdateEmployeeAvailabilityRequest;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.user.User;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeAvailabilityServiceImpl implements EmployeeAvailabilityService {
    @Override
    public List<EmployeeAvailability> getEmployeeAvailabilities(long shiftId, User user) throws ForbiddenException {
        throw new NotImplementedException();
    }

    @Override
    public EmployeeAvailability getEmployeeAvailability(long availabilityId, User user) throws ResourceNotFoundException, ForbiddenException {
        throw new NotImplementedException();
    }

    @Override
    public EmployeeAvailability createEmployeeAvailability(long shiftId, CreateEmployeeAvailabilityRequest request, User manager) throws ResourceNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public void deleteEmployeeAvailability(long availabilityId, User manager) throws ResourceNotFoundException {
        throw new NotImplementedException();
    }

    @Override
    public EmployeeAvailability updateEmployeeAvailability(long availabilityId, UpdateEmployeeAvailabilityRequest request, User manager) throws ResourceNotFoundException {
        throw new NotImplementedException();
    }
}
