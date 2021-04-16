package com.starsky.backend.service.user;

import com.starsky.backend.api.exception.InvalidInviteTokenException;
import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.user.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface UserService {
    User createUser(CreateUserRequest request) throws InvalidInviteTokenException;

    User getUserByEmail(String email);

    User getUserById(long id) throws ResourceNotFoundException;

    User getEmployeeById(long id, User owner) throws ResourceNotFoundException;

    List<User> getEmployees(User manager);
}
