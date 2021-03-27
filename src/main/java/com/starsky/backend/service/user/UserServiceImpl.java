package com.starsky.backend.service.user;

import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.NotificationType;
import com.starsky.backend.domain.Role;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.UserRepository;
import com.starsky.backend.service.invite.InviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final InviteService inviteService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, InviteService inviteService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.inviteService = inviteService;
    }

    @Override
    public User createUser(CreateUserRequest request) throws IllegalArgumentException {
        User user;
        Invite invite = null;
        if (request.getInviteToken() == null){
            user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle(),
                    null, true, NotificationType.EMAIL, Role.MANAGER, null);
        } else {
            invite = inviteService.findByToken(request.getInviteToken());
            if (invite == null){
                throw new IllegalArgumentException("Invite token does not exist.");
            }
            if (invite.getHasRegistered()){
                throw new IllegalArgumentException("Invite has already been used, user has already been registered.");
            }
            if (Duration.between(invite.getUpdatedAt(), LocalDateTime.now()).toDays() > 3){
                throw new IllegalArgumentException("Invite has expired - all invites have expiry date of 3 days.");
            }

            user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle(),
                    null, true, NotificationType.EMAIL, Role.EMPLOYEE, invite.getManager());
        }

        user = userRepository.save(user);
        if (invite != null){
            invite.setHasRegistered(true);
            inviteService.updateInvite(invite);
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
