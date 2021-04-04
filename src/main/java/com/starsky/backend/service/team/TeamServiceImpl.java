package com.starsky.backend.service.team;

import com.starsky.backend.domain.Role;
import com.starsky.backend.domain.Team;
import com.starsky.backend.domain.TeamMember;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.TeamMemberRepository;
import com.starsky.backend.repository.TeamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final Logger logger = LoggerFactory.getLogger(TeamServiceImpl.class);

    @Autowired
    public TeamServiceImpl(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
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
}
