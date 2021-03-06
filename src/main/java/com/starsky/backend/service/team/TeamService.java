package com.starsky.backend.service.team;

import com.starsky.backend.api.team.CreateTeamMemberRequest;
import com.starsky.backend.api.team.UpdateTeamRequest;
import com.starsky.backend.domain.team.Team;
import com.starsky.backend.domain.team.TeamMember;
import com.starsky.backend.domain.user.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;

import java.util.List;

public interface TeamService {
    List<Team> getTeams(User user);

    Team getTeam(long id, User owner) throws ResourceNotFoundException;

    List<TeamMember> getTeamMembers(long teamId, User user) throws ResourceNotFoundException;

    Team createTeam(String teamName, User owner) throws DataIntegrityViolationException;

    TeamMember createTeamMember(User member, Team team) throws DataIntegrityViolationException;

    Team updateTeam(long teamId, UpdateTeamRequest request, User owner) throws ResourceNotFoundException;

    void deleteTeam(long teamId, User owner) throws ResourceNotFoundException;

    void deleteTeamMember(long teamId, long employeeId, User owner) throws ResourceNotFoundException;

    void putAll(List<CreateTeamMemberRequest> requests, long teamId, User owner);
}
