package com.ye.ecust.services;

import com.ye.ecust.domain.Employee;
import com.ye.ecust.domain.SuggestResult;
import com.ye.ecust.repositories.EmployeeRepo;
import com.ye.ecust.repositories.RoleRepo;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by yesiyu on 2018/12/31.
 */
@Service
public class JobService {
    private final static Logger LOG = LoggerFactory.getLogger(JobService.class);

    private EmployeeRepo employeeRepo;
    private RoleRepo roleRepo;

    public JobService(EmployeeRepo employeeRepo,RoleRepo roleRepo) {
        this.employeeRepo = employeeRepo;
        this.roleRepo = roleRepo;
    }

    public Employee testFind(){
        System.out.print(employeeRepo.testFind("Employee 1"));
        return employeeRepo.testFind("Employee 1");
    }

    public List<Employee> excludeEmployee(String area, String competence){
        return employeeRepo.excludeEmployee(area, competence);
    }

    public Iterable<Map<String, Float>>excludeEmployeeUpdate(String teamNo){
        System.out.print(employeeRepo.excludeEmployeeUpdate(teamNo));
        return employeeRepo.excludeEmployeeUpdate(teamNo);
    }
}
