package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

/**
 * Created by yesiyu on 2018/12/31.
 */
public class Degree {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private int team_size;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTeam_size() {
        return team_size;
    }

    public void setTeam_size(int team_size) {
        this.team_size = team_size;
    }

    public Degree(String name, int team_size) {

        this.name = name;
        this.team_size = team_size;
    }
}
