package com.example.attendance.admin.dto;

import java.util.List;

import com.example.attendance.leave.dto.LeaveRequestResponse;

public record PendingRequestsResponse(
        List<LeaveRequestResponse> leaveRequests,
        List<AttendanceRequestResponse> attendanceRequests) {
}
