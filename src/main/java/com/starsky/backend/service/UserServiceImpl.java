package com.starsky.backend.service;

import com.starsky.backend.api.CreateUserRequest;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

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
    public User getUserByEmail(String email) {
        var user = userRepository.findByEmail(email);
        if (user == null){
            throw new ResourceNotFoundException("User with mail %s does not exist".formatted(email));
        }
        return user;
    }

    @Override
    public User createUser(CreateUserRequest request) {
        var user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle());
        return userRepository.save(user);
    }
}
