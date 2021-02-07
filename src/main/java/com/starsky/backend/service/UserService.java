package com.starsky.backend.service;

import com.starsky.backend.domain.User;

public interface UserService {
    User getUserByEmail(String email);
}
