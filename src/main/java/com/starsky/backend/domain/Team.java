package com.starsky.backend.domain;

import com.starsky.backend.api.team.TeamResponse;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team-id-generator")
    @SequenceGenerator(name = "team-id-generator", sequenceName = "team_sequence", allocationSize = 1)
    private long id;
    @NotNull
    private String name;
    @OneToOne
    @NotNull
    private User owner;

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
}


