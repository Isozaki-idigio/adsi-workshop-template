package com.example.attendance.leave;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestCreateRequest;
import com.example.attendance.leave.dto.LeaveRequestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LeaveServiceImpl implements LeaveService {

    private static final List<LeaveType> PAID_TYPES = List.of(LeaveType.PAID, LeaveType.HALF_DAY);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    public LeaveServiceImpl(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeRepository employeeRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public LeaveRequestResponse applyLeave(Long employeeId, LeaveRequestCreateRequest request) {
        BigDecimal days = calculateDays(request);

        if (request.leaveType() != LeaveType.SPECIAL) {
            var employee = employeeRepository.getReferenceById(employeeId);
            BigDecimal used = leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(
                    employeeId, PAID_TYPES);
            BigDecimal remaining = employee.getAnnualLeaveDays().subtract(used);

            if (remaining.compareTo(days) < 0) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE",
                        "有給残日数が不足しています（残: " + remaining + "日）");
            }
        }

        var leaveRequest = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveType(request.leaveType())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .days(days)
                .reason(request.reason())
                .status(ApprovalStatus.PENDING)
                .build();

        var saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    public LeaveBalanceResponse getBalance(Long employeeId) {
        var employee = employeeRepository.getReferenceById(employeeId);
        BigDecimal total = employee.getAnnualLeaveDays();
        BigDecimal used = leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(
                employeeId, PAID_TYPES);
        BigDecimal remaining = total.subtract(used);

        return new LeaveBalanceResponse(total, used, remaining);
    }

    @Override
    public List<LeaveRequestResponse> getRequests(Long employeeId, ApprovalStatus status) {
        List<LeaveRequest> requests;
        if (status != null) {
            requests = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
        } else {
            requests = leaveRequestRepository.findByEmployeeId(employeeId);
        }
        return requests.stream().map(this::toResponse).toList();
    }

    private BigDecimal calculateDays(LeaveRequestCreateRequest request) {
        if (request.leaveType() == LeaveType.HALF_DAY) {
            return new BigDecimal("0.5");
        }
        long daysBetween = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        return BigDecimal.valueOf(daysBetween);
    }

    private LeaveRequestResponse toResponse(LeaveRequest entity) {
        return new LeaveRequestResponse(
                entity.getId(),
                entity.getLeaveType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDays(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
