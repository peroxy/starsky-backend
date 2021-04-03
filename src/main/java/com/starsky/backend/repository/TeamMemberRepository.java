package com.starsky.backend.repository;

import com.starsky.backend.domain.TeamMember;
import com.starsky.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> getAllByMember(User user);
}
