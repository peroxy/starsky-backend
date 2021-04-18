package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.Schedule;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByTeamOwnerAndTeamId(User owner, long teamId);

    List<Schedule> findAllByTeamOwner(User owner);

    Optional<Schedule> findByIdAndTeamOwner(long id, User owner);
}
