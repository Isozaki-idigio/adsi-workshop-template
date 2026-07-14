package com.example.attendance.timerecord.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TimeRecordResponse(
        Long id,
        LocalDate workDate,
        LocalDateTime clockIn,
        LocalDateTime clockOut,
        long durationMinutes) {
}
