package com.starsky.backend.service;

import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.NotificationType;
import com.starsky.backend.domain.Role;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User createUser(CreateUserRequest request) {
        var user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle(),
                null, true, NotificationType.EMAIL, Role.MANAGER, null);
        return userRepository.save(user);
    }
}
