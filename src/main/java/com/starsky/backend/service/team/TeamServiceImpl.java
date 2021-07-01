package com.starsky.backend.service.team;

import com.starsky.backend.api.team.CreateTeamMemberRequest;
import com.starsky.backend.api.team.UpdateTeamRequest;
import com.starsky.backend.domain.team.Team;
import com.starsky.backend.domain.team.TeamMember;
import com.starsky.backend.domain.user.Role;
import com.starsky.backend.domain.user.User;
import com.starsky.backend.repository.TeamMemberRepository;
import com.starsky.backend.repository.TeamRepository;
import com.starsky.backend.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository, UserService userService) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userService = userService;
    }

    @Override
    public List<Team> getTeams(User user) {
        List<Team> teams;
        if (user.getRole() == Role.MANAGER) {
            teams = teamRepository.getAllByOwner(user);
        } else {
            teams = teamMemberRepository.getAllByMember(user).stream().map(TeamMember::getTeam).collect(Collectors.toList());
        }
        return teams;
    }

    @Override
    public Team getTeam(long id, User owner) throws ResourceNotFoundException {
        return teamRepository.findByIdAndOwnerId(id, owner.getId()).orElseThrow(() -> {
            var error = "Team (id=%d, owner=%d) does not exist.".formatted(id, owner.getId());
            this.logger.warn(error);
            return new ResourceNotFoundException(error);
        });
    }


    @Override
    public List<TeamMember> getTeamMembers(long teamId, User user) throws ResourceNotFoundException {
        Optional<Team> team;
        if (user.getParentUser() == null) {
            team = teamRepository.findByIdAndOwnerId(teamId, user.getId());
        } else {
            team = teamRepository.findByIdAndOwnerId(teamId, user.getParentUser().getId());
        }
        if (team.isPresent()) {
            return teamMemberRepository.getAllByTeam(team.get());
        }
        var error = "Team (id=%d) does not exist.".formatted(teamId);
        this.logger.warn(error);
        throw new ResourceNotFoundException(error);
    }

    @Override
    public Team createTeam(String teamName, User owner) {
        boolean exists = teamRepository.existsByOwnerAndName(owner, teamName);
        if (exists) {
            var error = "Key (team name)=(%s) already exists for user %s.".formatted(teamName, owner.getName());
            this.logger.warn(error);
            throw new DataIntegrityViolationException(error);
        }
        var team = new Team(teamName, owner);
        return teamRepository.save(team);
    }

    @Override
    public TeamMember createTeamMember(User member, Team team) {
        boolean exists = teamMemberRepository.existsByMemberAndTeam(member, team);
        if (exists) {
            var error = "Key (team member)=(%s) already exists in team %s.".formatted(member.getName(), team.getName());
            this.logger.warn(error);
            throw new DataIntegrityViolationException(error);
        }
        var teamMember = new TeamMember(member, team);
        return teamMemberRepository.save(teamMember);
    }

    @Override
    public Team updateTeam(long teamId, UpdateTeamRequest request, User owner) throws ResourceNotFoundException {
        var team = getTeam(teamId, owner);
        if (request.getName().isPresent()) {
            team.setName(request.getName().get());
        }
        return teamRepository.save(team);
    }

    @Override
    public void deleteTeam(long teamId, User owner) throws ResourceNotFoundException {
        var team = getTeam(teamId, owner);
        teamRepository.delete(team);
    }

    @Override
    public void deleteTeamMember(long teamId, long employeeId, User owner) throws ResourceNotFoundException {
        var members = getTeamMembers(teamId, owner);
        var member = members.stream().filter(teamMember -> teamMember.getMember().getId() == employeeId).findAny().orElseThrow(() -> {
            var error = "Team member (user id=%d) does not exist in team (id=%d).".formatted(employeeId, teamId);
            this.logger.warn(error);
            return new ResourceNotFoundException(error);
        });
        teamMemberRepository.delete(member);
    }

    @Override
    @Transactional
    public void putAll(List<CreateTeamMemberRequest> requests, long teamId, User owner) {
        var team = getTeam(teamId, owner);

        if (!userService.employeesExist(requests.stream().map(CreateTeamMemberRequest::getEmployeeId).toArray(Long[]::new), owner)) {
            var error = "One of the employee IDs does not exist or is not enabled.";
            this.logger.warn(error);
            throw new ResourceNotFoundException(error);
        }

        teamMemberRepository.deleteAllByTeamId(teamId);
        var teamMembers = new ArrayList<TeamMember>();
        for (var request : requests) {
            var user = new User();
            user.setId(request.getEmployeeId());
            teamMembers.add(new TeamMember(user, team));
        }
        teamMemberRepository.saveAll(teamMembers);

    }
}
