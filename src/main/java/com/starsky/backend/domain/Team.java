package com.starsky.backend.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "team-id-generator")
    @SequenceGenerator(name = "team-id-generator", sequenceName = "team-sequence")
    private long id;

    @NotNull
    private String name;

    @OneToOne
    private User owner;

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
}


