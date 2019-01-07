package com.ye.ecust.domain;

import java.util.List;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created by yesiyu on 2018/12/31.
 */
@NodeEntity
public class Employee {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @JsonIgnoreProperties("employee")
    @Relationship(type = "WORKS_AS", direction = Relationship.OUTGOING)
    private List<WorkingExperience> workingExperiences;

    public Employee(String name, List<WorkingExperience> workingExperiences) {
        this.name = name;
        this.workingExperiences = workingExperiences;
    }

    public List<WorkingExperience> getWorkingExperiences() {
        return workingExperiences;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
