package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.schedule.ScheduleShift;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                oneEmployeePerShift(constraintFactory),
                employeeIsAvailable(constraintFactory),
                employeeIsNotAvailable(constraintFactory),
                noOverlappingShiftsPerDay(constraintFactory),
                employeeHasTooManyShiftsAssigned(constraintFactory),
                oneShiftPerDay(constraintFactory),
                employeeHasTooManyHoursAssigned(constraintFactory),
                employeeHasLessThanMaxHoursAssigned(constraintFactory)
        };
    }

    // ############################################################################
    // Hard constraints
    // ############################################################################

    public Constraint oneEmployeePerShift(ConstraintFactory constraintFactory) {
        return constraintFactory.
                fromUniquePair(EmployeeAssignment.class,
                        Joiners.equal(EmployeeAssignment::getEmployee),
                        Joiners.equal(EmployeeAssignment::getShift))
                .penalize("one employee per shift", HardSoftScore.ONE_HARD);
    }

    public Constraint noOverlappingShiftsPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(EmployeeAssignment.class,
                        Joiners.equal(EmployeeAssignment::getEmployee),
                        Joiners.equal(EmployeeAssignment::getShiftDate))
                .penalize("no overlapping shifts in one day", HardSoftScore.ONE_HARD);
    }

    // ############################################################################
    // Soft constraints
    // ############################################################################

    public Constraint oneShiftPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .fromUniquePair(EmployeeAssignment.class,
                        Joiners.equal(EmployeeAssignment::getEmployee),
                        Joiners.equal(EmployeeAssignment::getShiftDayNumber))
                .penalize("one shift per day", HardSoftScore.ONE_SOFT);
    }

    public Constraint employeeIsAvailable(ConstraintFactory constraintFactory) {
        return constraintFactory.
                from(EmployeeAssignment.class).
                join(EmployeeAvailability.class, Joiners.equal(EmployeeAssignment::getEmployee, EmployeeAvailability::getEmployee))
                .filter((assignment, availability) -> availability.getShiftDate().isSubsetOfShift(assignment.getShiftDate()))
                .reward("employee is available", HardSoftScore.ONE_SOFT);
    }

    public Constraint employeeIsNotAvailable(ConstraintFactory constraintFactory) {
        return constraintFactory.
                from(EmployeeAssignment.class).
                join(EmployeeAvailability.class, Joiners.equal(EmployeeAssignment::getEmployee, EmployeeAvailability::getEmployee))
                .filter((assignment, availability) -> !availability.getShiftDate().isSubsetOfShift(assignment.getShiftDate()))
                .penalize("employee is not available", HardSoftScore.ONE_SOFT);
    }

    public Constraint employeeHasTooManyShiftsAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(EmployeeAssignment.class)
                .join(ScheduleShift.class)
                .filter((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee() != null)
                .groupBy((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee(),
                        (employeeAssignment, scheduleShift) -> scheduleShift,
                        ConstraintCollectors.countBi())
                .filter((user, scheduleShift, shiftCount) -> scheduleShift.getSchedule().getMaxShiftsPerEmployee() < shiftCount)
                .penalize("employee has more than max shifts assigned", HardSoftScore.ONE_SOFT, (user, scheduleShift, shiftCount) -> shiftCount - scheduleShift.getSchedule().getMaxShiftsPerEmployee());
    }

    public Constraint employeeHasLessThanMaxShiftsAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(EmployeeAssignment.class)
                .join(ScheduleShift.class)
                .filter((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee() != null)
                .groupBy((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee(),
                        (employeeAssignment, scheduleShift) -> scheduleShift,
                        ConstraintCollectors.countBi())
                .filter((user, scheduleShift, shiftCount) -> scheduleShift.getSchedule().getMaxShiftsPerEmployee() > shiftCount)
                .reward("employee has less than max shifts assigned", HardSoftScore.ONE_SOFT, (user, scheduleShift, shiftCount) -> scheduleShift.getSchedule().getMaxShiftsPerEmployee() - shiftCount);
    }

    public Constraint employeeHasTooManyHoursAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(EmployeeAssignment.class)
                .join(ScheduleShift.class)
                .filter((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee() != null)
                .groupBy((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee(), (employeeAssignment, scheduleShift) -> scheduleShift, ConstraintCollectors.sumLong((a, b) -> a.getHourDuration()))
                .filter((user, scheduleShift, totalHours) -> scheduleShift.getSchedule().getMaxHoursPerEmployee() < totalHours)
                .penalize("employee has more hours assigned than max hours per employee", HardSoftScore.ONE_SOFT, (user, scheduleShift, totalHours) -> totalHours.intValue() - scheduleShift.getSchedule().getMaxHoursPerEmployee());
    }

    public Constraint employeeHasLessThanMaxHoursAssigned(ConstraintFactory constraintFactory) {
        return constraintFactory
                .from(EmployeeAssignment.class)
                .join(ScheduleShift.class)
                .filter((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee() != null)
                .groupBy((employeeAssignment, scheduleShift) -> employeeAssignment.getEmployee(), (employeeAssignment, scheduleShift) -> scheduleShift, ConstraintCollectors.sumLong((a, b) -> a.getHourDuration()))
                .filter((user, scheduleShift, totalHours) -> scheduleShift.getSchedule().getMaxHoursPerEmployee() > totalHours)
                .reward("employee has less hours assigned than max hours per employee", HardSoftScore.ONE_SOFT, (user, scheduleShift, totalHours) -> scheduleShift.getSchedule().getMaxHoursPerEmployee() - totalHours.intValue());
    }

}
