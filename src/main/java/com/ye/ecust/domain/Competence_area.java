package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

/**
 * Created by yesiyu on 2018/12/31.
 */
public class Competence_area {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    public Competence_area(String name) {
        this.name = name;
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
}
