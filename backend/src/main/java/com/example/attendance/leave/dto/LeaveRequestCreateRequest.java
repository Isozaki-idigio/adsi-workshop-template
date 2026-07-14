package com.example.attendance.leave.dto;

import java.time.LocalDate;

import com.example.attendance.common.enums.LeaveType;

import jakarta.validation.constraints.NotNull;

public record LeaveRequestCreateRequest(
        @NotNull LeaveType leaveType,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String reason) {
}
