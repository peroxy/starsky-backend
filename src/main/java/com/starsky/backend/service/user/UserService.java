package com.starsky.backend.service.user;

import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.User;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

public interface UserService {
    User createUser(CreateUserRequest request) throws IllegalArgumentException;
    User getUserByEmail(String email);
}
