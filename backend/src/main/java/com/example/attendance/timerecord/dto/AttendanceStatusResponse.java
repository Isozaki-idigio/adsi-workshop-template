package com.example.attendance.timerecord.dto;

import java.util.List;

public record AttendanceStatusResponse(
        boolean isClockedIn,
        TimeRecordResponse currentRecord,
        List<TimeRecordResponse> todayRecords,
        long todayTotalMinutes) {
}
