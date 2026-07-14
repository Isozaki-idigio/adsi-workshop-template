package com.example.attendance.attendance.dto;

import java.time.LocalDateTime;

public record TimeRecordResponse(
        Long id,
        String workDate,
        LocalDateTime clockIn,
        LocalDateTime clockOut,
        long durationMinutes) {
}
