package com.example.attendance.admin.dto;

import com.example.attendance.common.dto.EmployeeResponse;

public record EmployeeAttendanceSummary(
        EmployeeResponse employee,
        long totalWorkMinutes,
        long totalOvertimeMinutes,
        int workDays) {
}
