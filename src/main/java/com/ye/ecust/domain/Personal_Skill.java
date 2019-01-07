package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

/**
 * Created by yesiyu on 2018/12/31.
 */
public class Personal_Skill {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String set;

    public Personal_Skill(String name, String set) {
        this.name = name;
        this.set = set;
    }

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

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }
}
