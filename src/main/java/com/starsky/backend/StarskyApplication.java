package com.starsky.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.starsky.backend.domain.invite.Invite;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.team.Team;
import com.starsky.backend.domain.team.TeamMember;
import com.starsky.backend.domain.user.NotificationType;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class StarskyApplication {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleShiftRepository scheduleShiftRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final InviteRepository inviteRepository;
    private final Environment environment;

    private final Logger logger = LoggerFactory.getLogger(StarskyApplication.class);

    @Autowired
    public StarskyApplication(UserRepository userRepository,
                              TeamRepository teamRepository,
                              TeamMemberRepository teamMemberRepository,
                              ScheduleRepository scheduleRepository,
                              ScheduleShiftRepository scheduleShiftRepository,
                              EmployeeAvailabilityRepository employeeAvailabilityRepository,
                              InviteRepository inviteRepository,
                              Environment environment) {
        this.userRepository = userRepository;
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleShiftRepository = scheduleShiftRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
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
                logger.info("Found test/dev profile");
                logger.info("Delete all data in tables and fill with mock data...");

                employeeAvailabilityRepository.deleteAllInBatch();
                scheduleShiftRepository.deleteAllInBatch();
                scheduleRepository.deleteAllInBatch();
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
                users.add(new User("Without Team", "no@team.com", bCryptPasswordEncoder().encode("password"), "Jobless",
                        null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(1)));
                users.add(new User("Scheduling test", "scheduling@a.com", bCryptPasswordEncoder().encode("password"), "Manager",
                        null, true, NotificationType.EMAIL, Role.MANAGER, null));

                for (int i = 0; i < 20; i++) {
                    users.add(new User("Scheduling user %d".formatted(i), "scheduling@%d.com".formatted(i), bCryptPasswordEncoder().encode("password"), "Waiter",
                            null, true, NotificationType.EMAIL, Role.EMPLOYEE, users.get(6)));
                }
                userRepository.saveAll(users);

                List<Team> teams = Arrays.asList(
                        new Team("Harold's Police Squad", users.get(0)),
                        new Team("Harold's Detectives", users.get(0)),
                        new Team("Test Team", users.get(1)),
                        new Team("Scheduling Test", users.get(6))
                );
                teamRepository.saveAll(teams);

                List<TeamMember> teamMembers = new ArrayList<>(Arrays.asList(
                        new TeamMember(users.get(2), teams.get(0)),
                        new TeamMember(users.get(3), teams.get(0)),
                        new TeamMember(users.get(4), teams.get(1)),
                        new TeamMember(users.get(4), teams.get(2))
                ));
                for (var user : users.subList(7, users.size() - 1)) {
                    teamMembers.add(new TeamMember(user, teams.get(3)));
                }
                teamMemberRepository.saveAll(teamMembers);


                List<Invite> invites = Arrays.asList(
                        new Invite(UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"), users.get(0), "David Michael Starsky", "david@starsky.com", false),
                        new Invite(UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a01"), users.get(0), "Kenneth Richard Hutchinson", "kenny@starsky.com", true),
                        new Invite(UUID.fromString("acaa86b2-ce32-4911-89b8-e1e2a1d39a05"), users.get(0), "Huggy Bear", "huggy@bear.com", false),
                        new Invite(UUID.fromString("bcaa86b2-ce32-4911-89b8-e1e2a1d39a06"), users.get(1), "Test Employee", "employee@test.com", false)
                );

                inviteRepository.saveAll(invites);

                var today = Instant.now().minus(Duration.ofDays(50)).truncatedTo(ChronoUnit.DAYS);
                var yesterday = today.minus(Duration.ofDays(1));
                List<Schedule> schedules = Arrays.asList(
                        new Schedule("Test schedule 1", today, today.plus(Duration.ofDays(5)), teams.get(3), 40, 5, 8),
                        new Schedule("Test schedule 2", yesterday, yesterday.plus(Duration.ofDays(7)), teams.get(3), 45, 6, 10)
                );
                scheduleRepository.saveAll(schedules);

                List<ScheduleShift> scheduleShifts = Arrays.asList(
                        new ScheduleShift(today.plus(Duration.ofHours(8)), today.plus(Duration.ofHours(16)), schedules.get(0), 2),
                        new ScheduleShift(today.plus(Duration.ofHours(16)), today.plus(Duration.ofHours(22)), schedules.get(0), 3),

                        new ScheduleShift(today.plus(Duration.ofDays(1)).plus(Duration.ofHours(8)),
                                today.plus(Duration.ofDays(1)).plus(Duration.ofHours(16)),
                                schedules.get(0),
                                1),
                        new ScheduleShift(today.plus(Duration.ofDays(1)).plus(Duration.ofHours(16)),
                                today.plus(Duration.ofDays(1)).plus(Duration.ofHours(22)),
                                schedules.get(0),
                                4),

                        new ScheduleShift(today.plus(Duration.ofDays(2)).plus(Duration.ofHours(7)),
                                today.plus(Duration.ofDays(2)).plus(Duration.ofHours(15)),
                                schedules.get(0),
                                3),
                        new ScheduleShift(today.plus(Duration.ofDays(2)).plus(Duration.ofHours(15)),
                                today.plus(Duration.ofDays(2)).plus(Duration.ofHours(22)),
                                schedules.get(0),
                                2),

                        new ScheduleShift(today.plus(Duration.ofDays(3)).plus(Duration.ofHours(8)),
                                today.plus(Duration.ofDays(3)).plus(Duration.ofHours(18)),
                                schedules.get(0),
                                5),

                        new ScheduleShift(today.plus(Duration.ofDays(4)).plus(Duration.ofHours(8)),
                                today.plus(Duration.ofDays(4)).plus(Duration.ofHours(16)),
                                schedules.get(0),
                                2),
                        new ScheduleShift(today.plus(Duration.ofDays(4)).plus(Duration.ofHours(16)),
                                today.plus(Duration.ofDays(4)).plus(Duration.ofHours(22)),
                                schedules.get(0),
                                5)
                );
                scheduleShiftRepository.saveAll(scheduleShifts);

                List<EmployeeAvailability> availabilities = new ArrayList<>();
                for (var employee : users.subList(7, users.size())) { //lol java toIndex is exclusive? chofl my dude, size - 1 UGH don't do it
                    for (var shift : scheduleShifts.subList(0, 5)) {
                        availabilities.add(new EmployeeAvailability(employee, shift, shift.getShiftStart(), shift.getShiftEnd(), 8));
                    }
                }
                employeeAvailabilityRepository.saveAll(availabilities);

                logger.info("Successfully inserted mock data");
            }
        };
    }


}
