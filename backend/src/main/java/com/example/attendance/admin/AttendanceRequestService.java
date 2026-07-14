package com.example.attendance.admin;

import java.util.List;

import com.example.attendance.admin.dto.AttendanceRequestCreateRequest;
import com.example.attendance.admin.dto.AttendanceRequestResponse;

public interface AttendanceRequestService {

    AttendanceRequestResponse submitRequest(Long employeeId, AttendanceRequestCreateRequest request);

    List<AttendanceRequestResponse> getRequests(Long employeeId);

    AttendanceRequestResponse approveRequest(Long approverId, Long requestId);

    AttendanceRequestResponse rejectRequest(Long approverId, Long requestId);

    List<AttendanceRequestResponse> getPendingByEmployeeIds(List<Long> employeeIds);
}
