package com.starsky.backend.service.schedule;

import com.starsky.backend.api.exception.DateRangeException;
import com.starsky.backend.api.exception.ForbiddenException;
import com.starsky.backend.api.schedule.CreateScheduleRequest;
import com.starsky.backend.api.schedule.ScheduleNotifyRequest;
import com.starsky.backend.api.schedule.UpdateScheduleRequest;
import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.EmployeeAssignmentRepository;
import com.starsky.backend.repository.ScheduleRepository;
import com.starsky.backend.service.team.TeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeamService teamService;
    private final DateRangeValidator dateRangeValidator;
    private final EmployeeAssignmentRepository employeeAssignmentRepository;
    private final String mailApiHostname;
    private final Logger logger = LoggerFactory.getLogger(ScheduleServiceImpl.class);
    private final String starskyHomeUrl;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, TeamService teamService, DateRangeValidator dateRangeValidator,
                               EmployeeAssignmentRepository employeeAssignmentRepository, @Value("${starsky.mail-api.host}") String mailApiHostname,
                               @Value("${starsky.frontend.register-url}") String frontendRegisterUrl) throws MalformedURLException {
        this.scheduleRepository = scheduleRepository;
        this.teamService = teamService;
        this.dateRangeValidator = dateRangeValidator;
        this.employeeAssignmentRepository = employeeAssignmentRepository;
        this.mailApiHostname = mailApiHostname;
        var url = new URL(frontendRegisterUrl);
        this.starskyHomeUrl = "%s://%s".formatted(url.getProtocol(), url.getHost());
    }

    @Override
    public List<Schedule> getSchedules(User user) {
        var manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }

        var schedules = scheduleRepository.findAllByTeamOwner(manager);

        if (user.getRole() == Role.EMPLOYEE) {
            var teams = teamService.getTeams(user);
            schedules.removeIf(schedule -> teams.stream().noneMatch(team -> team.getId() == schedule.getTeam().getId()));
        }

        return schedules;
    }

    @Override
    public Schedule getSchedule(long scheduleId, User user) throws ResourceNotFoundException, ForbiddenException {
        var manager = user;
        if (user.getRole() == Role.EMPLOYEE) {
            manager = user.getParentUser();
        }
        var schedule = scheduleRepository.findByIdAndTeamOwner(scheduleId, manager).orElseThrow(() -> {
            var error = "Schedule (id=%d) does not exist.".formatted(scheduleId);
            this.logger.warn(error);
            throw new ResourceNotFoundException(error);
        });

        if (user.getRole() == Role.EMPLOYEE) {
            var teams = teamService.getTeams(user);
            if (teams.stream().noneMatch(team -> team.getId() == schedule.getTeam().getId())) {
                var message =
                        "Authenticated user (id=%d) does not have necessary permissions to access this schedule - does not belong to schedule's team."
                                .formatted(user.getId());
                this.logger.warn(message);
                throw new ForbiddenException(message);
            }
        }

        return schedule;
    }

    @Override
    public List<Schedule> getSchedulesByTeam(User user, long teamId) {
        return scheduleRepository.findAllByTeamOwnerAndTeamId(user, teamId);
    }

    @Override
    public Schedule createSchedule(CreateScheduleRequest request, long teamId, User owner) throws DateRangeException, ResourceNotFoundException {
        dateRangeValidator.validateDateInterval(request.getScheduleStart(), request.getScheduleEnd());

        var team = teamService.getTeam(teamId, owner);

        var schedule = new Schedule(request.getScheduleName(),
                request.getScheduleStart(),
                request.getScheduleEnd(),
                team,
                request.getMaxHoursPerEmployee(),
                request.getMaxShiftsPerEmployee(),
                request.getMaxHoursPerShift());

        return scheduleRepository.save(schedule);
    }

    @Override
    public Schedule updateSchedule(UpdateScheduleRequest request, long scheduleId, User owner) throws DateRangeException, ResourceNotFoundException, ForbiddenException {
        var schedule = getSchedule(scheduleId, owner);
        if (request.getScheduleName().isPresent()) {
            schedule.setName(request.getScheduleName().get());
        }
        if (request.getScheduleStart().isPresent()) {
            schedule.setScheduleStart(request.getScheduleStart().get());
        }
        if (request.getScheduleEnd().isPresent()) {
            schedule.setScheduleEnd(request.getScheduleEnd().get());
        }
        if (request.getMaxHoursPerEmployee().isPresent()) {
            schedule.setMaxHoursPerEmployee(request.getMaxHoursPerEmployee().get());
        }
        if (request.getMaxHoursPerShift().isPresent()) {
            schedule.setMaxHoursPerShift(request.getMaxHoursPerShift().get());
        }
        if (request.getMaxShiftsPerEmployee().isPresent()) {
            schedule.setMaxShiftsPerEmployee(request.getMaxShiftsPerEmployee().get());
        }
        if (request.getTeamId().isPresent()) {
            var team = teamService.getTeam(request.getTeamId().get(), owner);
            schedule.setTeam(team);
        }
        dateRangeValidator.validateDateInterval(schedule.getScheduleStart(), schedule.getScheduleEnd());
        return scheduleRepository.save(schedule);
    }


    @Override
    public void deleteSchedule(long scheduleId, User owner) throws ResourceNotFoundException, ForbiddenException {
        getSchedule(scheduleId, owner); // will throw resource not found if not found
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    public void notifyEmployees(long scheduleId, User owner) throws ResourceNotFoundException, ForbiddenException {
        var schedule = getSchedule(scheduleId, owner); // will throw resource not found or forbidden
        var assignments = employeeAssignmentRepository.getAllByShiftSchedule(schedule);
        var employees = assignments.stream().collect(Collectors.toMap(employeeAssignment -> employeeAssignment.getEmployee().getId(), p -> p, (p, q) -> p))
                .values().stream().map(EmployeeAssignment::getEmployee).collect(Collectors.toSet());

        var client = WebClient.create(mailApiHostname);
        String scheduleDate = "%s - %s".formatted(formatToDate(schedule.getScheduleStart()), formatToDate(schedule.getScheduleEnd()));
        for (var employee : employees) {
            var shiftBuilder = new StringBuilder();
            var shifts = assignments.stream().filter(employeeAssignment -> employeeAssignment.getEmployee().getId() == employee.getId()).map(EmployeeAssignment::getShift)
                    .collect(Collectors.toMap(ScheduleShift::getId, p -> p, (p, q) -> p)).values();
            for (var shift : shifts) {
                shiftBuilder.append(formatToDateTime(shift.getShiftStart()));
                shiftBuilder.append(" - ");
                shiftBuilder.append(formatToDateTime(shift.getShiftEnd()));
                shiftBuilder.append(";\n");
            }
            var body = new ScheduleNotifyRequest(owner.getName(), employee.getName(), employee.getEmail(), starskyHomeUrl, scheduleDate, shiftBuilder.toString());
            this.logger.info("Sending request to mail-api: {}", body);
            var response = client.post().uri("/schedule-notifications").contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve().toBodilessEntity().block();
            this.logger.info("Response code from starsky mail-api: {}", response.getStatusCode());
        }
    }

    private String formatToDate(Instant date) {
        return DateTimeFormatter.ofPattern("d.M.yyyy").withZone(ZoneId.of("UTC")).format(date);
    }

    private String formatToDateTime(Instant date) {
        return DateTimeFormatter.ofPattern("d.M. HH:mm").withZone(ZoneId.of("UTC")).format(date);
    }

}
