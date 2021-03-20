package com.starsky.backend;

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
    CommandLineRunner runner() {
        return args -> {
            if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
                teamMemberRepository.deleteAllInBatch();
                teamRepository.deleteAllInBatch();
                inviteRepository.deleteAllInBatch();
                userRepository.deleteAllInBatch();


                List<User> users = new ArrayList<>(Arrays.asList(
                        new User("John Doe", "john@doe.com", bCryptPasswordEncoder().encode("password"), "School Manager",
                                null, true, NotificationType.EMAIL, Role.MANAGER, null),
                        new User("Test Manager", "a@a.com", bCryptPasswordEncoder().encode("password"), "Test Manager",
                                null, true, NotificationType.EMAIL, Role.MANAGER, null)
                ));

                users.add(new User("Mother Theresa", "mother@t.com", bCryptPasswordEncoder().encode("password"), "Teacher",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(0)));
                users.add(new User("Will Smith", "w@s.com", bCryptPasswordEncoder().encode("password"), "Animator",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(0)));
                users.add(new User("Test Employee", "t@t.com", bCryptPasswordEncoder().encode("password"), "Animator",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(1)));

                userRepository.saveAll(users);

                List<Team> teams = Arrays.asList(
                        new Team("John's Team", users.get(0)),
                        new Team("Test Team", users.get(1))
                );
                teamRepository.saveAll(teams);

                List<TeamMember> teamMembers = Arrays.asList(
                        new TeamMember(users.get(2), teams.get(0)),
                        new TeamMember(users.get(3), teams.get(0)),
                        new TeamMember(users.get(4), teams.get(1))
                );
                teamMemberRepository.saveAll(teamMembers);
            }
        };
    }


}
