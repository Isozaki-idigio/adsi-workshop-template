package com.example.attendance.timerecord.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceResponse(
        LocalDate date,
        List<TimeRecordResponse> records,
        long totalMinutes,
        long overtimeMinutes,
        boolean isNightWork) {
}
