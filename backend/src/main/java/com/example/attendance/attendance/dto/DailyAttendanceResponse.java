package com.example.attendance.attendance.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceResponse(
        LocalDate date,
        List<TimeRecordResponse> records,
        long totalMinutes,
        long overtimeMinutes,
        long nightMinutes) {
}
