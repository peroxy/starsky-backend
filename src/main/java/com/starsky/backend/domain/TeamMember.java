package com.starsky.backend.domain;

import javax.persistence.*;

@Entity
public class TeamMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team-member-id-generator")
    @SequenceGenerator(name = "team-member-id-generator", sequenceName = "team-member-sequence")
    private long id;

    @OneToOne
    private User member;

    @OneToOne
    private Team team;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public User getMember() {
        return member;
    }

    public void setMember(User member) {
        this.member = member;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
