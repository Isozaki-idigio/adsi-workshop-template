package com.example.attendance.leave;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            var employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
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
    @Transactional(readOnly = true)
    public LeaveBalanceResponse getBalance(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
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

    @Override
    @Transactional
    public LeaveRequestResponse approveLeave(Long approverId, Long requestId) {
        var leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "休暇申請が見つかりません"));

        verifyDepartmentAccess(approverId, leaveRequest.getEmployeeId());

        if (leaveRequest.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "ALREADY_PROCESSED", "この申請は既に処理済みです");
        }

        leaveRequest.setStatus(ApprovalStatus.APPROVED);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        var saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public LeaveRequestResponse rejectLeave(Long approverId, Long requestId) {
        var leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "休暇申請が見つかりません"));

        verifyDepartmentAccess(approverId, leaveRequest.getEmployeeId());

        if (leaveRequest.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "ALREADY_PROCESSED", "この申請は既に処理済みです");
        }

        leaveRequest.setStatus(ApprovalStatus.REJECTED);
        leaveRequest.setApproverId(approverId);
        leaveRequest.setApprovedAt(LocalDateTime.now());

        var saved = leaveRequestRepository.save(leaveRequest);
        return toResponse(saved);
    }

    private void verifyDepartmentAccess(Long approverId, Long targetEmployeeId) {
        var approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "承認者が見つかりません"));
        var target = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "対象社員が見つかりません"));

        if (!approver.getDepartmentId().equals(target.getDepartmentId())) {
            throw new BusinessException(
                    HttpStatus.FORBIDDEN, "DEPARTMENT_MISMATCH", "他部署の申請は操作できません");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveRequestResponse> getPendingByEmployeeIds(List<Long> employeeIds) {
        return leaveRequestRepository.findByEmployeeIdInAndStatus(employeeIds, ApprovalStatus.PENDING)
                .stream().map(this::toResponse).toList();
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
