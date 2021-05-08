package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.EmployeeAvailability;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailability, Long> {
    Optional<EmployeeAvailability> getEmployeeAvailabilityByIdAndShiftScheduleTeamOwner(long availabilityId, User owner);
}
