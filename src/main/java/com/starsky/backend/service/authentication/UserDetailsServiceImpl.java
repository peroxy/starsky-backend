package com.starsky.backend.service.authentication;

import com.starsky.backend.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            var user = userService.getUserByEmailAndManuallyAdded(email, false);
            List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority("ROLE_%s".formatted(user.getRole().name())));
            return new User(user.getEmail(), user.getPassword(), roles);
        } catch (ResourceNotFoundException ex) {
            throw new UsernameNotFoundException(email);
        }
    }
}
