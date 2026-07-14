package com.example.attendance.timerecord;

import com.example.attendance.timerecord.dto.AttendanceStatusResponse;
import com.example.attendance.timerecord.dto.MonthlyAttendanceResponse;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import java.time.YearMonth;

public interface AttendanceService {

    TimeRecordResponse clockIn(Long employeeId);

    TimeRecordResponse clockOut(Long employeeId);

    AttendanceStatusResponse getStatus(Long employeeId);

    MonthlyAttendanceResponse getRecords(Long employeeId, YearMonth yearMonth);
}
