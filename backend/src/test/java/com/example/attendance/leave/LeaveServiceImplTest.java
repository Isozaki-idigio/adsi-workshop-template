package com.example.attendance.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.attendance.leave.dto.LeaveRequestCreateRequest;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    private LeaveService leaveService;

    @BeforeEach
    void setUp() {
        leaveService = new LeaveServiceImpl(leaveRequestRepository, employeeRepository);
    }

    @Test
    @DisplayName("有給を申請すると日数が計算されPENDINGで作成される")
    void applyLeave_paid_createsWithCorrectDays() {
        var employee = Employee.builder()
                .id(1L)
                .annualLeaveDays(new BigDecimal("12.0"))
                .build();
        when(employeeRepository.getReferenceById(1L)).thenReturn(employee);
        when(leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(any(), any()))
                .thenReturn(new BigDecimal("3.0"));
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest lr = invocation.getArgument(0);
                    lr.setId(1L);
                    return lr;
                });

        var request = new LeaveRequestCreateRequest(
                LeaveType.PAID,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                "旅行");

        var result = leaveService.applyLeave(1L, request);

        assertThat(result.days()).isEqualByComparingTo(new BigDecimal("3.0"));
        assertThat(result.status()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(result.leaveType()).isEqualTo(LeaveType.PAID);
    }

    @Test
    @DisplayName("半休を申請すると0.5日で作成される")
    void applyLeave_halfDay_createsWith05Days() {
        var employee = Employee.builder()
                .id(1L)
                .annualLeaveDays(new BigDecimal("12.0"))
                .build();
        when(employeeRepository.getReferenceById(1L)).thenReturn(employee);
        when(leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest lr = invocation.getArgument(0);
                    lr.setId(1L);
                    return lr;
                });

        var request = new LeaveRequestCreateRequest(
                LeaveType.HALF_DAY,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                null);

        var result = leaveService.applyLeave(1L, request);

        assertThat(result.days()).isEqualByComparingTo(new BigDecimal("0.5"));
    }

    @Test
    @DisplayName("残日数不足で申請するとBusinessExceptionが投げられる")
    void applyLeave_insufficientBalance_throwsException() {
        var employee = Employee.builder()
                .id(1L)
                .annualLeaveDays(new BigDecimal("12.0"))
                .build();
        when(employeeRepository.getReferenceById(1L)).thenReturn(employee);
        when(leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(any(), any()))
                .thenReturn(new BigDecimal("11.5"));

        var request = new LeaveRequestCreateRequest(
                LeaveType.PAID,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                null);

        assertThatThrownBy(() -> leaveService.applyLeave(1L, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("特別休暇は有給残日数を消化しない")
    void applyLeave_special_doesNotConsumeBalance() {
        when(leaveRequestRepository.save(any(LeaveRequest.class)))
                .thenAnswer(invocation -> {
                    LeaveRequest lr = invocation.getArgument(0);
                    lr.setId(1L);
                    return lr;
                });

        var request = new LeaveRequestCreateRequest(
                LeaveType.SPECIAL,
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 1),
                "慶弔");

        var result = leaveService.applyLeave(1L, request);

        assertThat(result.leaveType()).isEqualTo(LeaveType.SPECIAL);
        assertThat(result.days()).isEqualByComparingTo(new BigDecimal("1.0"));
    }

    @Test
    @DisplayName("残日数が正しく計算される")
    void getBalance_returnsCorrectValues() {
        var employee = Employee.builder()
                .id(1L)
                .annualLeaveDays(new BigDecimal("12.0"))
                .build();
        when(employeeRepository.getReferenceById(1L)).thenReturn(employee);
        when(leaveRequestRepository.sumApprovedDaysByEmployeeIdAndLeaveTypes(any(), any()))
                .thenReturn(new BigDecimal("3.5"));

        var result = leaveService.getBalance(1L);

        assertThat(result.totalDays()).isEqualByComparingTo(new BigDecimal("12.0"));
        assertThat(result.usedDays()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(result.remainingDays()).isEqualByComparingTo(new BigDecimal("8.5"));
    }
}
