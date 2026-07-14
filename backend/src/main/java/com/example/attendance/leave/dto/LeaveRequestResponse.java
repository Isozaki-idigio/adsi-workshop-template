package com.example.attendance.leave.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;

public record LeaveRequestResponse(
        Long id,
        LeaveType leaveType,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal days,
        String reason,
        ApprovalStatus status,
        LocalDateTime createdAt) {
}
