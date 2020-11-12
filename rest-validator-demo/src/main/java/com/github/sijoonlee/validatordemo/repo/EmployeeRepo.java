package com.github.sijoonlee.validatordemo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.sijoonlee.validatordemo.model.Employee;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long>{

}
