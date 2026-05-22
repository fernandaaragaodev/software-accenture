package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    List<Employee> findAll();

    Optional<Employee> findByDisplayNameIgnoreCase(String displayName);

    List<Employee> findByTeamId(Long teamId);

    Employee save(Employee employee);

    void saveAll(List<Employee> employees);
}
