package com.starsky.backend.service.schedule.solve;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
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
        };
    }

    private Constraint oneEmployeePerShift(ConstraintFactory constraintFactory) {
        return constraintFactory.
                fromUniquePair(EmployeeAssignment.class,
                        Joiners.equal(EmployeeAssignment::getEmployee),
                        Joiners.equal(EmployeeAssignment::getShift))
                .penalize("one employee per shift", HardSoftScore.ONE_HARD);
    }
}
