package com.example.attendance.employee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByDepartmentIdAndActiveTrue(Long departmentId);
}
