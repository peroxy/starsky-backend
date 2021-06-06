package com.starsky.backend.api.schedule;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.service.schedule.solve.ScheduleConstraintProvider;
import com.starsky.backend.service.schedule.solve.SolvedSchedule;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;

@SpringBootTest
public class ScheduleConstraintProviderTest {

    @Autowired
    ConstraintVerifier<ScheduleConstraintProvider, SolvedSchedule> constraintVerifier;

    @Test
    public void testOneEmployeePerShiftConstraint() {
        var employee = Mockito.mock(User.class);
        var employee2 = Mockito.mock(User.class);
        var shift = Mockito.mock(ScheduleShift.class);

        var assignment = new EmployeeAssignment(employee, shift, Instant.parse("2020-01-01T08:00:00Z"), Instant.parse("2020-01-01T16:00:00Z"));
        var conflictingAssignment = new EmployeeAssignment(employee, shift, Instant.parse("2020-01-01T08:00:00Z"), Instant.parse("2020-01-01T16:00:00Z"));
        var nonConflictingAssignment = new EmployeeAssignment(employee2, shift, Instant.parse("2020-01-01T08:00:00Z"), Instant.parse("2020-01-01T16:00:00Z"));
        constraintVerifier.verifyThat(ScheduleConstraintProvider::oneEmployeePerShift)
                .given(assignment, conflictingAssignment, nonConflictingAssignment).penalizes(1);
    }

    @Test
    public void testEmployeeIsAvailableConstraint() {
        var start = Instant.parse("2020-01-01T08:00:00Z");
        var end = Instant.parse("2020-01-01T16:00:00Z");
        var employee = Mockito.mock(User.class);
        var employee2 = Mockito.mock(User.class);
        var employee3 = Mockito.mock(User.class);
        var shift = Mockito.mock(ScheduleShift.class);
        Mockito.when(shift.getShiftStart()).thenReturn(start);
        Mockito.when(shift.getShiftEnd()).thenReturn(end);
        Mockito.when(shift.getNumberOfRequiredEmployees()).thenReturn(3);
        Mockito.when(shift.getEmployeeAvailabilities()).thenReturn(Arrays.asList(
                new EmployeeAvailability(employee, shift, start, end, 8),
                new EmployeeAvailability(employee3, shift, start.plus(Duration.ofHours(4)), end.plus(Duration.ofHours(4)), 8),
                new EmployeeAvailability(employee2, shift, start, end, 8)));

        var availableAssignment = new EmployeeAssignment(employee, shift, start, end);
        var availableAssignment2 = new EmployeeAssignment(employee2, shift, start, end);
        var notAvailableAssignment = new EmployeeAssignment(employee3, shift, start, end);

        var solvedSchedule = new SolvedSchedule(1, Collections.singletonList(shift), Arrays.asList(employee, employee2, employee3), Arrays.asList(availableAssignment, availableAssignment2, notAvailableAssignment));

        constraintVerifier.verifyThat(ScheduleConstraintProvider::employeeIsAvailable)
                .givenSolution(solvedSchedule)
                .rewards(2);
    }

    @Test
    public void testEmployeeIsNotAvailableConstraint() {
        var start = Instant.parse("2020-01-01T08:00:00Z");
        var end = Instant.parse("2020-01-01T16:00:00Z");
        var employee = Mockito.mock(User.class);
        var employee2 = Mockito.mock(User.class);
        var employee3 = Mockito.mock(User.class);
        var shift = Mockito.mock(ScheduleShift.class);
        Mockito.when(shift.getShiftStart()).thenReturn(start);
        Mockito.when(shift.getShiftEnd()).thenReturn(end);
        Mockito.when(shift.getNumberOfRequiredEmployees()).thenReturn(4);
        Mockito.when(shift.getEmployeeAvailabilities()).thenReturn(Arrays.asList(
                new EmployeeAvailability(employee, shift, start.plus(Duration.ofHours(4)), end.plus(Duration.ofHours(4)), 8),
                new EmployeeAvailability(employee3, shift, start, end, 8),
                new EmployeeAvailability(employee2, shift, start.plus(Duration.ofHours(4)), end.plus(Duration.ofHours(4)), 8)));

        var assignment = new EmployeeAssignment(employee, shift, start, end);
        var assignment2 = new EmployeeAssignment(employee2, shift, start, end);
        var availableAssignment = new EmployeeAssignment(employee3, shift, start, end);

        var solvedSchedule = new SolvedSchedule(1, Collections.singletonList(shift), Arrays.asList(employee, employee2, employee3),
                Arrays.asList(assignment, assignment2, availableAssignment));

        constraintVerifier.verifyThat(ScheduleConstraintProvider::employeeIsNotAvailable)
                .givenSolution(solvedSchedule)
                .penalizes(2);
    }
}
