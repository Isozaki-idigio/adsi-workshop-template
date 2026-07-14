package com.example.attendance.timerecord;

import java.time.YearMonth;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.timerecord.dto.AttendanceStatusResponse;
import com.example.attendance.timerecord.dto.MonthlyAttendanceResponse;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping("/clock-in")
    public ResponseEntity<TimeRecordResponse> clockIn() {
        Long employeeId = getAuthenticatedEmployeeId();
        var response = attendanceService.clockIn(employeeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/clock-out")
    public ResponseEntity<TimeRecordResponse> clockOut() {
        Long employeeId = getAuthenticatedEmployeeId();
        var response = attendanceService.clockOut(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status")
    public ResponseEntity<AttendanceStatusResponse> getStatus() {
        Long employeeId = getAuthenticatedEmployeeId();
        var response = attendanceService.getStatus(employeeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/records")
    public ResponseEntity<MonthlyAttendanceResponse> getRecords(@RequestParam String yearMonth) {
        Long employeeId = getAuthenticatedEmployeeId();
        YearMonth ym = YearMonth.parse(yearMonth);
        var response = attendanceService.getRecords(employeeId, ym);
        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedEmployeeId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "認証が必要です");
        }
        return (Long) auth.getPrincipal();
    }
}
