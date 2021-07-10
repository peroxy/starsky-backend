package com.starsky.backend.service.user;

import com.starsky.backend.api.exception.InvalidInviteTokenException;
import com.starsky.backend.api.user.CreateEmployeeRequest;
import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.api.user.UpdateEmployeeRequest;
import com.starsky.backend.api.user.UpdateUserRequest;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface UserService {
    User createUser(CreateUserRequest request) throws InvalidInviteTokenException;

    User createEmployee(CreateEmployeeRequest request, User manager);

    User updateEmployee(UpdateEmployeeRequest request, User manager, long employeeId);

    User getUserByEmail(String email) throws ResourceNotFoundException;

    User getUserByEmailAndManuallyAdded(String email, boolean manuallyAdded) throws ResourceNotFoundException;

    User getUserById(long id) throws ResourceNotFoundException;

    User getEmployeeById(long id, User owner) throws ResourceNotFoundException;

    List<User> getEmployees(User manager);

    User updateUser(User user, UpdateUserRequest request);

    boolean employeesExist(Long[] employeeIds, User owner);
}
