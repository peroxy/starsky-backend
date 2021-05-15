package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailability, Long> {
    Optional<EmployeeAvailability> getEmployeeAvailabilityByIdAndShiftScheduleTeamOwner(long availabilityId, User owner);


    @Query(value = "select ea.* from {h-schema}employee_availability ea " +
            "join {h-schema}schedule_shift ss on ss.id = ea.shift_id " +
            "where ea.employee_id = :employee_id " +
            "and ss.id = :shift_id " +
            "and (:availability_start >= ea.availability_start and :availability_start <= ea.availability_end " +
            "or ea.availability_start >= :availability_end and ea.availability_end <= :availability_end) " +
            "limit 1",
            nativeQuery = true)
    List<EmployeeAvailability> findAllByEmployeeIdAndShiftIdAndAvailabilityBetween(@Param("employee_id") long employeeId,
                                                                                   @Param("shift_id") long shiftId,
                                                                                   @Param("availability_start") Instant availabilityStart,
                                                                                   @Param("availability_end") Instant availabilityEnd);
}
