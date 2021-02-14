package com.starsky.backend.service;

import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.User;

public interface UserService {
    User createUser(CreateUserRequest request);
}
