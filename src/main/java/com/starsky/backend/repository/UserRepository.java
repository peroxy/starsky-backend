package com.starsky.backend.repository;

import com.starsky.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(exported = false)
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndEnabled(String email, boolean enabled);

    Optional<User> findByIdAndEnabled(long id, boolean enabled);

    List<User> findAllByParentUserAndEnabled(User parentUser, boolean enabled);

    Optional<User> findByIdAndParentUserAndEnabled(long id, User owner, boolean enabled);
}

