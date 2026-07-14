package com.example.attendance.common.dto;

import com.example.attendance.common.enums.Role;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        String departmentName,
        Role role) {
}
