package com.example.attendance.timerecord.dto;

import java.util.List;

public record MonthlyAttendanceResponse(
        String yearMonth,
        long totalWorkMinutes,
        long totalOvertimeMinutes,
        List<DailyAttendanceResponse> records) {
}
