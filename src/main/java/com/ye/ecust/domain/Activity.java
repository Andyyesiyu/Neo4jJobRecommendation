package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;

/**
 * Created by yesiyu on 2018/12/31.
 */
public class Activity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private float complexity;

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

    public float getComplexity() {
        return complexity;
    }

    public void setComplexity(float complexity) {
        this.complexity = complexity;
    }

    public Activity(String name, float complexity) {

        this.name = name;
        this.complexity = complexity;
    }
}
