package com.starsky.backend.service.team;

import com.starsky.backend.domain.Team;
import com.starsky.backend.domain.User;

import java.util.List;

public interface TeamService {
    List<Team> getTeams(User user);
    Team createTeam(String teamName, User owner);
}
