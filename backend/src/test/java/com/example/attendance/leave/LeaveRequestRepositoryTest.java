package com.example.attendance.leave;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.department.Department;
import com.example.attendance.department.DepartmentRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LeaveRequestRepositoryTest {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        var dept = departmentRepository.save(
                Department.builder().name("テスト部_" + System.nanoTime()).build());
        employee = employeeRepository.save(Employee.builder()
                .employeeCode("LV_" + System.nanoTime())
                .name("テスト社員")
                .email("leave_" + System.nanoTime() + "@example.com")
                .passwordHash("$2a$10$dummy")
                .departmentId(dept.getId())
                .role(Role.EMPLOYEE)
                .build());
    }

    @Test
    @DisplayName("休暇申請を保存して取得できる")
    void save_and_findById() {
        var request = LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .days(new BigDecimal("1.0"))
                .status(ApprovalStatus.PENDING)
                .build();

        var saved = leaveRequestRepository.save(request);

        assertThat(saved.getId()).isNotNull();
        assertThat(leaveRequestRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("社員IDで申請一覧を取得できる")
    void findByEmployeeId_returnsRequests() {
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .days(new BigDecimal("1.0"))
                .status(ApprovalStatus.PENDING)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.HALF_DAY)
                .startDate(LocalDate.of(2026, 8, 5))
                .endDate(LocalDate.of(2026, 8, 5))
                .days(new BigDecimal("0.5"))
                .status(ApprovalStatus.APPROVED)
                .build());

        List<LeaveRequest> results = leaveRequestRepository.findByEmployeeId(employee.getId());

        assertThat(results).hasSize(2);
    }

    @Test
    @DisplayName("ステータスでフィルタできる")
    void findByEmployeeIdAndStatus_filtersCorrectly() {
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 1))
                .days(new BigDecimal("1.0"))
                .status(ApprovalStatus.PENDING)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 8, 10))
                .endDate(LocalDate.of(2026, 8, 10))
                .days(new BigDecimal("1.0"))
                .status(ApprovalStatus.APPROVED)
                .build());

        var pending = leaveRequestRepository.findByEmployeeIdAndStatus(
                employee.getId(), ApprovalStatus.PENDING);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    @Test
    @DisplayName("承認済み有給の合計日数を集計できる")
    void sumApprovedDays_returnsCorrectTotal() {
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.PAID)
                .startDate(LocalDate.of(2026, 7, 1))
                .endDate(LocalDate.of(2026, 7, 2))
                .days(new BigDecimal("2.0"))
                .status(ApprovalStatus.APPROVED)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.HALF_DAY)
                .startDate(LocalDate.of(2026, 7, 5))
                .endDate(LocalDate.of(2026, 7, 5))
                .days(new BigDecimal("0.5"))
                .status(ApprovalStatus.APPROVED)
                .build());
        leaveRequestRepository.save(LeaveRequest.builder()
                .employeeId(employee.getId())
                .leaveType(LeaveType.SPECIAL)
                .startDate(LocalDate.of(2026, 7, 10))
                .endDate(LocalDate.of(2026, 7, 10))
                .days(new BigDecimal("1.0"))
                .status(ApprovalStatus.APPROVED)
                .build());

        BigDecimal total = leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(
                employee.getId(), List.of(LeaveType.PAID, LeaveType.HALF_DAY));

        assertThat(total).isEqualByComparingTo(new BigDecimal("2.5"));
    }
}
