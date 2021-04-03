package com.starsky.backend.service.user;

import com.starsky.backend.api.exception.InvalidInviteTokenException;
import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.User;

import java.util.List;

public interface UserService {
    User createUser(CreateUserRequest request) throws InvalidInviteTokenException;

    User getUserByEmail(String email);

    List<User> getEmployees(User manager);
}
