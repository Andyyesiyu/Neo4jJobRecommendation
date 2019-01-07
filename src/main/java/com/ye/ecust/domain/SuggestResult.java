package com.ye.ecust.domain;

/**
 * Created by yesiyu on 2019/1/5.
 */
public class SuggestResult {
    private String Candidate;
    private float score;

    public SuggestResult(String employeeName, float score) {
        this.Candidate = employeeName;
        this.score = score;
    }

    public String getEmployeeName() {
        return Candidate;
    }

    public void setEmployee(String employee) {
        this.Candidate = employee;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
