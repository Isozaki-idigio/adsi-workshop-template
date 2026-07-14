package com.example.attendance.attendance;

import java.time.YearMonth;

import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping("/records")
    public ResponseEntity<MonthlyAttendanceResponse> getRecords(
            Authentication auth,
            @RequestParam String yearMonth) {
        Long employeeId = (Long) auth.getPrincipal();
        YearMonth ym = YearMonth.parse(yearMonth);
        var response = attendanceService.getMonthlyRecords(employeeId, ym);
        return ResponseEntity.ok(response);
    }
}
