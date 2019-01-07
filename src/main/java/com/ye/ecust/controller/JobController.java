package com.ye.ecust.controller;

import com.ye.ecust.domain.Employee;
import com.ye.ecust.domain.SuggestResult;
import com.ye.ecust.services.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by yesiyu on 2018/12/31.
 */

@RestController
@RequestMapping("/")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }



    @RequestMapping(value="/init")
    public Employee TestControl(){
        return jobService.testFind();
    }

    @RequestMapping(value="/exclude")
    public List<Employee> excludeEmployee(@RequestParam String area, @RequestParam String competence){
        return jobService.excludeEmployee(area, competence);
    }

    @RequestMapping(value="/excludeUpdate")
    public Iterable<Map<String, Float>>excludeEmployeeUpdate(@RequestParam String teamNo){
        return jobService.excludeEmployeeUpdate(teamNo);
    }
}

