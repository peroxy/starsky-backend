package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.EmployeeAssignment;
import com.starsky.backend.domain.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface EmployeeAssignmentRepository extends JpaRepository<EmployeeAssignment, Long> {
    List<EmployeeAssignment> getAllByShiftSchedule(Schedule schedule);

    Optional<EmployeeAssignment> findByIdAndShiftSchedule(long assignmentId, Schedule schedule);

    void deleteAllByShiftSchedule(Schedule schedule);
}
