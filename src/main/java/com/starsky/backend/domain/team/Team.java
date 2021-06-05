package com.starsky.backend.domain.team;

import com.starsky.backend.api.team.TeamResponse;
import com.starsky.backend.domain.BaseEntity;
import com.starsky.backend.domain.user.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team-id-generator")
    @SequenceGenerator(name = "team-id-generator", sequenceName = "team_sequence")
    private long id;
    @NotNull
    private String name;
    @OneToOne
    @NotNull
    private User owner;
    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers;

    public Team(@NotNull String name, @NotNull User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Team() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public TeamResponse toResponse() {
        return new TeamResponse(id, name, owner.getName());
    }

    public List<TeamMember> getTeamMembers() {
        return teamMembers;
    }
}


