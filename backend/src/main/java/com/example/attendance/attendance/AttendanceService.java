package com.example.attendance.attendance;

import java.time.YearMonth;

import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;

public interface AttendanceService {

    MonthlyAttendanceResponse getMonthlyRecords(Long employeeId, YearMonth yearMonth);
}
