package com.starsky.backend.service.team;

import com.starsky.backend.domain.team.Team;
import com.starsky.backend.domain.team.TeamMember;
import com.starsky.backend.domain.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface TeamService {
    List<Team> getTeams(User user);

    Team getTeam(long id, User owner) throws ResourceNotFoundException;

    List<TeamMember> getTeamMembers(long teamId) throws ResourceNotFoundException;

    Team createTeam(String teamName, User owner) throws DataIntegrityViolationException;

    TeamMember createTeamMember(User member, Team team) throws DataIntegrityViolationException;
}
