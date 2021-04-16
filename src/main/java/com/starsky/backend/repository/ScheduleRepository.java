package com.starsky.backend.repository;

import com.starsky.backend.domain.schedule.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(exported = false)
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
