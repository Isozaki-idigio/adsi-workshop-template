package com.example.attendance.employee;

import com.example.attendance.common.dto.EmployeeResponse;

public interface EmployeeService {

    EmployeeResponse authenticate(String employeeCode, String password);

    EmployeeResponse getById(Long employeeId);
}
