package com.starsky.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.domain.*;
import com.starsky.backend.repository.InviteRepository;
import com.starsky.backend.repository.TeamMemberRepository;
import com.starsky.backend.repository.TeamRepository;
import com.starsky.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class StarskyApplication {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final InviteRepository inviteRepository;
    private final Environment environment;

    @Autowired
    public StarskyApplication(UserRepository userRepository, TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, InviteRepository inviteRepository, Environment environment) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.inviteRepository = inviteRepository;
        this.environment = environment;
    }

    public static void main(String[] args) {
        SpringApplication.run(StarskyApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public ObjectMapper mapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        return objectMapper;
    }


    @Bean
    CommandLineRunner runner() {
        return args -> {
            var profiles = Arrays.asList(environment.getActiveProfiles());
            if (profiles.contains("dev") || profiles.contains("test")) {
                teamMemberRepository.deleteAllInBatch();
                teamRepository.deleteAllInBatch();
                inviteRepository.deleteAllInBatch();
                userRepository.deleteAllInBatch();

                // Don't modify the values as they are used in testing.. make sure to run tests if you change the mock data

                List<User> users = new ArrayList<>(Arrays.asList(
                        new User("Harold C. Dobey", "mail@example.com", bCryptPasswordEncoder().encode("password"), "Police Captain",
                                null, true, NotificationType.EMAIL, Role.MANAGER, null),
                        new User("Test Manager", "a@a.com", bCryptPasswordEncoder().encode("password"), "Test Manager",
                                null, true, NotificationType.EMAIL, Role.MANAGER, null)
                ));

                users.add(new User("David Starsky", "david@starsky.net", bCryptPasswordEncoder().encode("password"), "Police Detective",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(0)));
                users.add(new User("Kenneth Hutchinson", "kenneth@starsky.net", bCryptPasswordEncoder().encode("password"), "Police Detective",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(0)));
                users.add(new User("Test Employee", "t@t.com", bCryptPasswordEncoder().encode("password"), "Animator",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(1)));

                userRepository.saveAll(users);

                List<Team> teams = Arrays.asList(
                        new Team("Harold's Police Squad", users.get(0)),
                        new Team("Test Team", users.get(1))
                );
                teamRepository.saveAll(teams);

                List<TeamMember> teamMembers = Arrays.asList(
                        new TeamMember(users.get(2), teams.get(0)),
                        new TeamMember(users.get(3), teams.get(0)),
                        new TeamMember(users.get(4), teams.get(1))
                );
                teamMemberRepository.saveAll(teamMembers);


                List<Invite> invites = Arrays.asList(
                        new Invite(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"), users.get(0), "David Michael Starsky", "david@starsky.com", false),
                        new Invite(UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a01"), users.get(0), "Kenneth Richard Hutchinson", "kenny@starsky.com", true),
                        new Invite(UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a05"), users.get(0), "Huggy Bear", "huggy@bear.com", false)
                );

                inviteRepository.saveAll(invites);
            }
        };
    }


}
