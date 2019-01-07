package com.ye.ecust.domain;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

/**
 * Created by yesiyu on 2018/12/31.
 */
public class Role {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String dept;
    private String hierarchy;
    private Long open_status;

    @Relationship(type = "WORKS_AS", direction = Relationship.INCOMING)
    private List<WorkingExperience> workingExperiences;


    public Role(String name, String dept, String hierarchy, Long open_status, List<WorkingExperience> workingExperiences) {
        this.name = name;
        this.dept = dept;
        this.hierarchy = hierarchy;
        this.open_status = open_status;
        this.workingExperiences = workingExperiences;
    }

    public List<WorkingExperience> getWorkingExperiences() {
        return workingExperiences;
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

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setOpen_status(Long open_status) {
        this.open_status = open_status;
    }

    public String getDept() {
        return dept;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public Long getOpen_status() {
        return open_status;
    }
}
