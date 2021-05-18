package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.schedule.ScheduleShift;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface ScheduleShiftRepository extends JpaRepository<ScheduleShift, Long> {
    List<ScheduleShift> getAllByScheduleAndScheduleTeamOwner(Schedule schedule, User owner);

    Optional<ScheduleShift> getByIdAndScheduleTeamOwner(long shiftId, User owner);

    @Query(value = "select ss.* from {h-schema}schedule_shift ss " +
            "where ss.schedule_id = :schedule_id " +
            "and (:shift_start >= ss.shift_start and :shift_start <= ss.shift_end " +
            "or ss.shift_start >= :shift_end and ss.shift_end <= :shift_end) ",
            nativeQuery = true)
    List<ScheduleShift> findAllByScheduleIdAndShiftBetween(@Param("schedule_id") long scheduleId,
                                                           @Param("shift_start") Instant shiftStart,
                                                           @Param("shift_end") Instant shiftEnd);
}
