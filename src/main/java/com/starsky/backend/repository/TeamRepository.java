package com.starsky.backend.repository;

import com.starsky.backend.domain.team.Team;
import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> getAllByOwner(User owner);

    Optional<Team> findByIdAndOwner(long teamId, User owner);

    boolean existsByOwnerAndName(User owner, String name);
}