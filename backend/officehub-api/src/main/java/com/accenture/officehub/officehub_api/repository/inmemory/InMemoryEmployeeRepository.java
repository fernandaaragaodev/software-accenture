package com.accenture.officehub.officehub_api.repository.inmemory;

import com.accenture.officehub.officehub_api.model.Employee;
import com.accenture.officehub.officehub_api.repository.EmployeeRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final List<Employee> employees = new CopyOnWriteArrayList<>();

    @Override
    public List<Employee> findAll() {
        return employees.stream().sorted(Comparator.comparing(Employee::getId)).map(this::copy).toList();
    }

    @Override
    public Optional<Employee> findByDisplayNameIgnoreCase(String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return Optional.empty();
        }
        String needle = displayName.trim().toLowerCase(Locale.ROOT);
        return employees.stream()
                .filter(e -> e.getDisplayName() != null && e.getDisplayName().trim().toLowerCase(Locale.ROOT).equals(needle))
                .findFirst()
                .map(this::copy);
    }

    @Override
    public List<Employee> findByTeamId(Long teamId) {
        return employees.stream()
                .filter(e -> e.getTeamId() != null && e.getTeamId().equals(teamId))
                .map(this::copy)
                .toList();
    }

    @Override
    public Employee save(Employee employee) {
        employees.removeIf(e -> e.getId().equals(employee.getId()));
        Employee copy = copy(employee);
        employees.add(copy);
        return copy(copy);
    }

    @Override
    public void saveAll(List<Employee> newEmployees) {
        employees.clear();
        newEmployees.stream().map(this::copy).forEach(employees::add);
    }

    private Employee copy(Employee e) {
        return new Employee(
                e.getId(),
                e.getDisplayName(),
                e.getTeamId(),
                e.getProfessionalProfile(),
                e.isHidePresenceFromTeam(),
                e.getTypicalStartTime()
        );
    }
}
