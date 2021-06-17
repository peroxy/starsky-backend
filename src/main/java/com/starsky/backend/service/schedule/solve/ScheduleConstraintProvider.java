package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.EmployeeAvailability;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;
import org.optaplanner.core.api.score.stream.Joiners;

public class ScheduleConstraintProvider implements ConstraintProvider {
    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                oneEmployeePerShift(constraintFactory),
                employeeIsAvailable(constraintFactory),
                employeeIsNotAvailable(constraintFactory),

        };
    }

    public Constraint oneEmployeePerShift(ConstraintFactory constraintFactory) {
        return constraintFactory.
                fromUniquePair(EmployeeAssignment.class,
                        Joiners.equal(EmployeeAssignment::getEmployee),
                        Joiners.equal(EmployeeAssignment::getShift))
                .penalize("one employee per shift", HardSoftScore.ONE_HARD);
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

}
