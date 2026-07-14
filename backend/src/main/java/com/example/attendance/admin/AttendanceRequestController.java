package com.example.attendance.admin;

import java.util.List;

import com.example.attendance.admin.dto.AttendanceRequestCreateRequest;
import com.example.attendance.admin.dto.AttendanceRequestResponse;
import com.example.attendance.common.exception.BusinessException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance-requests")
public class AttendanceRequestController {

    private final AttendanceRequestService attendanceRequestService;

    public AttendanceRequestController(AttendanceRequestService attendanceRequestService) {
        this.attendanceRequestService = attendanceRequestService;
    }

    @PostMapping
    public ResponseEntity<AttendanceRequestResponse> submitRequest(
            @Valid @RequestBody AttendanceRequestCreateRequest request) {
        Long employeeId = getAuthenticatedEmployeeId();
        var response = attendanceRequestService.submitRequest(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<AttendanceRequestResponse>> getRequests() {
        Long employeeId = getAuthenticatedEmployeeId();
        var response = attendanceRequestService.getRequests(employeeId);
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
