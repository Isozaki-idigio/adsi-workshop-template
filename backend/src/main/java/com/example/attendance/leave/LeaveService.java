package com.example.attendance.leave;

import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;

public interface LeaveService {

    LeaveRequestResponse applyLeave(Long employeeId, LeaveRequestCreateRequest request);

    LeaveBalanceResponse getBalance(Long employeeId);

    List<LeaveRequestResponse> getRequests(Long employeeId, ApprovalStatus status);

    LeaveRequestResponse approveLeave(Long approverId, Long requestId);

    LeaveRequestResponse rejectLeave(Long approverId, Long requestId);

    List<LeaveRequestResponse> getPendingByEmployeeIds(List<Long> employeeIds);
}
