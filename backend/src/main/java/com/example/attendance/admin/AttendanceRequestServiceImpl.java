package com.example.attendance.admin;

import java.time.LocalDateTime;
import java.util.List;

import com.example.attendance.admin.dto.AttendanceRequestCreateRequest;
import com.example.attendance.admin.dto.AttendanceRequestResponse;
import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.AttendanceRequestType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceRequestServiceImpl implements AttendanceRequestService {

    private final AttendanceRequestRepository attendanceRequestRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceRequestServiceImpl(
            AttendanceRequestRepository attendanceRequestRepository,
            TimeRecordRepository timeRecordRepository,
            EmployeeRepository employeeRepository) {
        this.attendanceRequestRepository = attendanceRequestRepository;
        this.timeRecordRepository = timeRecordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceRequestResponse submitRequest(Long employeeId, AttendanceRequestCreateRequest request) {
        var entity = AttendanceRequest.builder()
                .employeeId(employeeId)
                .timeRecordId(request.timeRecordId())
                .requestType(request.requestType())
                .workDate(request.workDate())
                .requestedClockIn(request.requestedClockIn())
                .requestedClockOut(request.requestedClockOut())
                .reason(request.reason())
                .status(ApprovalStatus.PENDING)
                .build();

        var saved = attendanceRequestRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequestResponse> getRequests(Long employeeId) {
        return attendanceRequestRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AttendanceRequestResponse approveRequest(Long approverId, Long requestId) {
        var request = attendanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "申請が見つかりません"));

        verifyDepartmentAccess(approverId, request.getEmployeeId());

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "ALREADY_PROCESSED", "この申請は既に処理済みです");
        }

        request.setStatus(ApprovalStatus.APPROVED);
        request.setApproverId(approverId);
        request.setApprovedAt(LocalDateTime.now());

        applyToTimeRecord(request);

        var saved = attendanceRequestRepository.save(request);
        return toResponse(saved);
    }

    @Override
    public AttendanceRequestResponse rejectRequest(Long approverId, Long requestId) {
        var request = attendanceRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "REQUEST_NOT_FOUND", "申請が見つかりません"));

        verifyDepartmentAccess(approverId, request.getEmployeeId());

        if (request.getStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(
                    HttpStatus.CONFLICT, "ALREADY_PROCESSED", "この申請は既に処理済みです");
        }

        request.setStatus(ApprovalStatus.REJECTED);
        request.setApproverId(approverId);
        request.setApprovedAt(LocalDateTime.now());

        var saved = attendanceRequestRepository.save(request);
        return toResponse(saved);
    }

    private void applyToTimeRecord(AttendanceRequest request) {
        if (request.getRequestType() == AttendanceRequestType.ADD) {
            var newRecord = TimeRecord.builder()
                    .employeeId(request.getEmployeeId())
                    .workDate(request.getWorkDate())
                    .clockIn(request.getRequestedClockIn())
                    .clockOut(request.getRequestedClockOut())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .version(0L)
                    .build();
            timeRecordRepository.save(newRecord);
        } else if (request.getRequestType() == AttendanceRequestType.MODIFY) {
            var record = timeRecordRepository.findById(request.getTimeRecordId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.NOT_FOUND, "RECORD_NOT_FOUND", "打刻記録が見つかりません"));
            record.setClockIn(request.getRequestedClockIn());
            record.setClockOut(request.getRequestedClockOut());
            record.setUpdatedAt(LocalDateTime.now());
            timeRecordRepository.save(record);
        } else if (request.getRequestType() == AttendanceRequestType.DELETE) {
            if (request.getTimeRecordId() != null) {
                timeRecordRepository.deleteById(request.getTimeRecordId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRequestResponse> getPendingByEmployeeIds(List<Long> employeeIds) {
        return attendanceRequestRepository.findByEmployeeIdInAndStatus(employeeIds, ApprovalStatus.PENDING)
                .stream().map(this::toResponse).toList();
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

    private AttendanceRequestResponse toResponse(AttendanceRequest entity) {
        return new AttendanceRequestResponse(
                entity.getId(),
                entity.getRequestType(),
                entity.getWorkDate(),
                entity.getRequestedClockIn(),
                entity.getRequestedClockOut(),
                entity.getReason(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
