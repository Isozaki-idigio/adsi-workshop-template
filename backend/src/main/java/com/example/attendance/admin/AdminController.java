package com.example.attendance.admin;

import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import com.example.attendance.admin.dto.EmployeeAttendanceSummary;
import com.example.attendance.admin.dto.PendingRequestsResponse;
import com.example.attendance.admin.dto.TimeRecordModifyRequest;
import com.example.attendance.common.dto.EmployeeResponse;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.vo.WorkDuration;
import com.example.attendance.department.DepartmentRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.leave.LeaveService;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.admin.dto.AttendanceRequestResponse;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeRecordRepository timeRecordRepository;
    private final LeaveService leaveService;
    private final AttendanceRequestService attendanceRequestService;

    public AdminController(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            TimeRecordRepository timeRecordRepository,
            LeaveService leaveService,
            AttendanceRequestService attendanceRequestService) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.timeRecordRepository = timeRecordRepository;
        this.leaveService = leaveService;
        this.attendanceRequestService = attendanceRequestService;
    }

    @GetMapping("/department/attendance")
    public ResponseEntity<List<EmployeeAttendanceSummary>> getDepartmentAttendance(
            @RequestParam String yearMonth) {
        var manager = getAuthenticatedManager();
        var employees = employeeRepository.findByDepartmentIdAndActiveTrue(manager.getDepartmentId());
        YearMonth ym = YearMonth.parse(yearMonth);

        List<EmployeeAttendanceSummary> summaries = employees.stream()
                .map(emp -> buildSummary(emp, ym))
                .toList();

        return ResponseEntity.ok(summaries);
    }

    @PutMapping("/attendance/{employeeId}/{recordId}")
    public ResponseEntity<TimeRecordResponse> modifyEmployeeAttendance(
            @PathVariable Long employeeId,
            @PathVariable Long recordId,
            @Valid @RequestBody TimeRecordModifyRequest request) {
        var manager = getAuthenticatedManager();
        verifyDepartmentAccess(manager, employeeId);

        var record = timeRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "RECORD_NOT_FOUND", "打刻記録が見つかりません"));

        if (!record.getEmployeeId().equals(employeeId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "この操作は許可されていません");
        }

        record.setClockIn(request.clockIn());
        record.setClockOut(request.clockOut());
        var saved = timeRecordRepository.save(record);

        long duration = 0;
        if (saved.getClockOut() != null) {
            duration = Duration.between(saved.getClockIn(), saved.getClockOut()).toMinutes();
        }
        return ResponseEntity.ok(new TimeRecordResponse(
                saved.getId(), saved.getWorkDate(), saved.getClockIn(), saved.getClockOut(), duration));
    }

    @GetMapping("/pending-requests")
    public ResponseEntity<PendingRequestsResponse> getPendingRequests() {
        var manager = getAuthenticatedManager();
        List<Long> employeeIds = employeeRepository
                .findByDepartmentIdAndActiveTrue(manager.getDepartmentId())
                .stream().map(Employee::getId).toList();

        var leaveRequests = leaveService.getPendingByEmployeeIds(employeeIds);
        var attendanceRequests = getAttendanceRequestsByEmployeeIds(employeeIds);

        return ResponseEntity.ok(new PendingRequestsResponse(leaveRequests, attendanceRequests));
    }

    @PostMapping("/leave-requests/{requestId}/approve")
    public ResponseEntity<LeaveRequestResponse> approveLeaveRequest(@PathVariable Long requestId) {
        var manager = getAuthenticatedManager();
        var response = leaveService.approveLeave(manager.getId(), requestId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leave-requests/{requestId}/reject")
    public ResponseEntity<LeaveRequestResponse> rejectLeaveRequest(@PathVariable Long requestId) {
        var manager = getAuthenticatedManager();
        var response = leaveService.rejectLeave(manager.getId(), requestId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendance-requests/{requestId}/approve")
    public ResponseEntity<AttendanceRequestResponse> approveAttendanceRequest(@PathVariable Long requestId) {
        var manager = getAuthenticatedManager();
        var response = attendanceRequestService.approveRequest(manager.getId(), requestId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/attendance-requests/{requestId}/reject")
    public ResponseEntity<AttendanceRequestResponse> rejectAttendanceRequest(@PathVariable Long requestId) {
        var manager = getAuthenticatedManager();
        var response = attendanceRequestService.rejectRequest(manager.getId(), requestId);
        return ResponseEntity.ok(response);
    }

    private Employee getAuthenticatedManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "認証が必要です");
        }
        Long employeeId = (Long) auth.getPrincipal();
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "認証が必要です"));

        if (employee.getRole() != Role.MANAGER) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "管理者権限が必要です");
        }
        return employee;
    }

    private void verifyDepartmentAccess(Employee manager, Long targetEmployeeId) {
        var target = employeeRepository.findById(targetEmployeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "社員が見つかりません"));

        if (!target.getDepartmentId().equals(manager.getDepartmentId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "FORBIDDEN", "他部門の社員は操作できません");
        }
    }

    private EmployeeAttendanceSummary buildSummary(Employee employee, YearMonth yearMonth) {
        var records = timeRecordRepository.findByEmployeeIdAndWorkDateBetween(
                employee.getId(), yearMonth.atDay(1), yearMonth.atEndOfMonth());

        long totalMinutes = records.stream()
                .filter(r -> r.getClockOut() != null)
                .mapToLong(r -> Duration.between(r.getClockIn(), r.getClockOut()).toMinutes())
                .sum();

        var workDuration = new WorkDuration(Duration.ofMinutes(totalMinutes));
        int workDays = (int) records.stream()
                .map(TimeRecord::getWorkDate)
                .distinct()
                .count();

        var deptName = departmentRepository.findById(employee.getDepartmentId())
                .map(d -> d.getName()).orElse("");

        var empResponse = new EmployeeResponse(
                employee.getId(), employee.getEmployeeCode(), employee.getName(),
                employee.getEmail(), deptName, employee.getRole());

        return new EmployeeAttendanceSummary(empResponse, totalMinutes, workDuration.overtimeMinutes(), workDays);
    }

    private List<AttendanceRequestResponse> getAttendanceRequestsByEmployeeIds(List<Long> employeeIds) {
        return attendanceRequestService.getPendingByEmployeeIds(employeeIds);
    }
}
