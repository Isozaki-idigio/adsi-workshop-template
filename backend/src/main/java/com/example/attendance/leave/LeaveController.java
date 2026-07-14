package com.example.attendance.leave;

import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/requests")
    public ResponseEntity<LeaveRequestResponse> applyLeave(
            Authentication auth,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        Long employeeId = (Long) auth.getPrincipal();
        var response = leaveService.applyLeave(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<LeaveRequestResponse>> getRequests(
            Authentication auth,
            @RequestParam(required = false) ApprovalStatus status) {
        Long employeeId = (Long) auth.getPrincipal();
        var responses = leaveService.getRequests(employeeId, status);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/balance")
    public ResponseEntity<LeaveBalanceResponse> getBalance(Authentication auth) {
        Long employeeId = (Long) auth.getPrincipal();
        var balance = leaveService.getBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
}
