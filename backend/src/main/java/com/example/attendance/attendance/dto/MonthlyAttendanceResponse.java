package com.example.attendance.attendance.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        String yearMonth,
        long totalWorkMinutes,
        long totalOvertimeMinutes,
        long totalNightMinutes,
        List<DailyAttendanceResponse> days) {
}
