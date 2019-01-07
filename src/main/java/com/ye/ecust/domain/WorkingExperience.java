package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.*;
import org.neo4j.ogm.annotation.RelationshipEntity;

/**
 * Created by yesiyu on 2019/1/1.
 */
@RelationshipEntity(type = "WORKS_AS")
public class WorkingExperience {
    @Id
    @GeneratedValue
    private Long id;
    private Long duration;
    private String location;

    @StartNode
    private Employee employee;

    @EndNode
    private Role role;

    public WorkingExperience(Employee employee, Role role) {
        this.employee = employee;
        this.role = role;
    }

    public Long getDuration() {
        return duration;
    }

    public String getLocation() {
        return location;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }
}
