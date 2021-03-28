package com.starsky.backend.repository;

import com.starsky.backend.domain.Invite;
import com.starsky.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(exported = false)
public interface InviteRepository extends JpaRepository<Invite, Long> {
    Invite findByEmployeeEmail(String email);

    Invite findByToken(UUID token);

    List<Invite> findAllByManager(User manager);
}
