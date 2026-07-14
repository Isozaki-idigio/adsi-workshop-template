package com.example.attendance.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.AttendanceRequestType;

public record AttendanceRequestResponse(
        Long id,
        AttendanceRequestType requestType,
        LocalDate workDate,
        LocalDateTime requestedClockIn,
        LocalDateTime requestedClockOut,
        String reason,
        ApprovalStatus status,
        LocalDateTime createdAt) {
}
