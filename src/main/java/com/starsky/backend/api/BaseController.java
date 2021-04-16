package com.starsky.backend.api;

import com.starsky.backend.domain.user.User;
import com.starsky.backend.service.user.UserService;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {
    private final UserService userService;

    public BaseController(UserService userService) {
        this.userService = userService;
    }

    protected User getAuthenticatedUser() {
        var email = getAuthenticatedEmail();
        return userService.getUserByEmail(email);
    }

    protected String getAuthenticatedEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
