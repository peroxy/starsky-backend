package com.starsky.backend.service.team;

import com.starsky.backend.domain.Role;
import com.starsky.backend.domain.Team;
import com.starsky.backend.domain.TeamMember;
import com.starsky.backend.domain.User;
import com.starsky.backend.repository.TeamMemberRepository;
import com.starsky.backend.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

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
}
