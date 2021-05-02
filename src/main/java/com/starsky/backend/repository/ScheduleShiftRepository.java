package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface ScheduleShiftRepository extends JpaRepository<ScheduleShift, Long> {
    List<ScheduleShift> getAllByScheduleAndScheduleTeamOwner(Schedule schedule, User owner);

    Optional<ScheduleShift> getByIdAndScheduleTeamOwner(long shiftId, User owner);
}
