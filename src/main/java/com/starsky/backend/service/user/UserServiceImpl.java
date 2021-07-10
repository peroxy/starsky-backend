package com.starsky.backend.service.user;

import com.starsky.backend.api.exception.InvalidInviteTokenException;
import com.starsky.backend.api.user.CreateEmployeeRequest;
import com.starsky.backend.api.user.CreateUserRequest;
import com.starsky.backend.api.user.UpdateEmployeeRequest;
import com.starsky.backend.api.user.UpdateUserRequest;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.user.NotificationType;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.UserRepository;
import com.starsky.backend.service.invite.InviteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final InviteService inviteService;
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, InviteService inviteService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.inviteService = inviteService;
    }

    @Override
    public User createUser(CreateUserRequest request) throws InvalidInviteTokenException {
        User user;
        Invite invite = null;
        if (request.getInviteToken() == null) {
            user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle(),
                    null, true, NotificationType.EMAIL, Role.MANAGER, null, false);
        } else {
            invite = inviteService.getByToken(request.getInviteToken());
            var validation = inviteService.validateInvite(invite);
            if (validation.hasError()) {
                throw new InvalidInviteTokenException(request.getInviteToken(), validation.getError());
            }

            user = new User(request.getName(), request.getEmail(), bCryptPasswordEncoder.encode(request.getPassword()), request.getJobTitle(),
                    null, true, NotificationType.EMAIL, Role.EMPLOYEE, invite.getManager(), false);
        }

        user = userRepository.save(user);
        if (invite != null) {
            invite.setHasRegistered(true);
            inviteService.updateInvite(invite);
        }
        return user;
    }

    @Override
    public User createEmployee(CreateEmployeeRequest request, User manager) {
        var user = new User(request.getName(),
                request.getEmail(),
                null,
                request.getJobTitle(),
                null,
                true,
                NotificationType.EMAIL,
                Role.EMPLOYEE,
                manager,
                true);
        user = userRepository.save(user);
        return user;
    }

    @Override
    public User updateEmployee(UpdateEmployeeRequest request, User manager, long employeeId) {
        var employee = getEmployeeById(employeeId, manager);

        if (request.getEmail().isPresent()) {
            employee.setEmail(request.getEmail().get());
        }
        if (request.getName().isPresent()) {
            employee.setName(request.getName().get());
        }
        if (request.getJobTitle().isPresent()) {
            employee.setJobTitle(request.getJobTitle().get());
        }

        return userRepository.save(employee);
    }

    @Override
    public User getUserByEmail(String email) throws ResourceNotFoundException {
        var user = userRepository.findByEmailAndEnabled(email, true);
        if (user.isPresent()) {
            return user.get();
        }
        var error = "User (email=%s) does not exist.".formatted(email);
        this.logger.warn(error);
        throw new ResourceNotFoundException(error);
    }

    public User getUserByEmailAndManuallyAdded(String email, boolean manuallyAdded) throws ResourceNotFoundException {
        var user = userRepository.findByEmailAndEnabledAndManuallyAdded(email, true, manuallyAdded);
        if (user.isPresent()) {
            return user.get();
        }
        var error = "User (email=%s) does not exist.".formatted(email);
        this.logger.warn(error);
        throw new ResourceNotFoundException(error);
    }

    @Override
    public User getUserById(long id) throws ResourceNotFoundException {
        var user = userRepository.findByIdAndEnabled(id, true);
        if (user.isPresent()) {
            return user.get();
        }
        var error = "User (id=%d) does not exist.".formatted(id);
        this.logger.warn(error);
        throw new ResourceNotFoundException(error);
    }

    @Override
    public User getEmployeeById(long id, User owner) throws ResourceNotFoundException {
        var employee = userRepository.findByIdAndParentUserAndEnabled(id, owner, true);
        if (employee.isPresent()) {
            return employee.get();
        }
        var error = "Employee (id=%d, owner=%d) does not exist.".formatted(id, owner.getId());
        this.logger.warn(error);
        throw new ResourceNotFoundException(error);
    }

    @Override
    public List<User> getEmployees(User manager) {
        return userRepository.findAllByParentUserAndEnabled(manager, true);
    }

    @Override
    public User updateUser(User user, UpdateUserRequest request) {
        if (request.getName().isPresent()) {
            user.setName(request.getName().get());
        }
        if (request.getEmail().isPresent()) {
            user.setEmail(request.getEmail().get());
        }
        if (request.getJobTitle().isPresent()) {
            user.setJobTitle(request.getJobTitle().get());
        }
        if (request.getPassword().isPresent()) {
            user.setPassword(bCryptPasswordEncoder.encode(request.getPassword().get()));
        }
        return userRepository.save(user);
    }

    @Override
    public boolean employeesExist(Long[] employeeIds, User owner) {
        return userRepository.existsAllByIdInAndParentUserAndEnabled(employeeIds, owner, true);
    }

    @Override
    public void deleteEmployee(long employeeId, User manager) throws ResourceNotFoundException {
        var employee = getEmployeeById(employeeId, manager);
        employee.setEnabled(false);
        userRepository.save(employee);
    }

}
