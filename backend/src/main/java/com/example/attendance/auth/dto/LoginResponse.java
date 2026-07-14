package com.example.attendance.auth.dto;

import com.example.attendance.common.dto.EmployeeResponse;

public record LoginResponse(
        String token,
        EmployeeResponse employee) {
}
