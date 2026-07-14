package com.example.attendance.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.example.attendance.admin.dto.AttendanceRequestCreateRequest;
import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.AttendanceRequestType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.Employee;
import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceRequestServiceImplTest {

    @Mock
    private AttendanceRequestRepository attendanceRequestRepository;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    @Mock
    private com.example.attendance.employee.EmployeeRepository employeeRepository;

    private AttendanceRequestService service;

    @BeforeEach
    void setUp() {
        service = new AttendanceRequestServiceImpl(attendanceRequestRepository, timeRecordRepository, employeeRepository);
    }

    private void mockSameDepartment() {
        var approver = Employee.builder().id(99L).departmentId(1L).build();
        var target = Employee.builder().id(1L).departmentId(1L).build();
        when(employeeRepository.findById(99L)).thenReturn(Optional.of(approver));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(target));
    }

    @Test
    @DisplayName("打刻修正申請が作成される")
    void submitRequest_createsRequest() {
        var request = new AttendanceRequestCreateRequest(
                AttendanceRequestType.MODIFY, 1L,
                LocalDate.of(2026, 7, 14),
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                "打刻忘れ");

        when(attendanceRequestRepository.save(any())).thenAnswer(inv -> {
            AttendanceRequest saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var result = service.submitRequest(1L, request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(result.reason()).isEqualTo("打刻忘れ");
    }

    @Test
    @DisplayName("承認するとTimeRecordが更新される(MODIFY)")
    void approveRequest_modify_updatesTimeRecord() {
        var request = AttendanceRequest.builder()
                .id(1L).employeeId(1L).timeRecordId(10L)
                .requestType(AttendanceRequestType.MODIFY)
                .workDate(LocalDate.of(2026, 7, 14))
                .requestedClockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .requestedClockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .reason("修正").status(ApprovalStatus.PENDING).version(0L)
                .build();

        var timeRecord = TimeRecord.builder()
                .id(10L).employeeId(1L).workDate(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 10, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 19, 0))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).version(0L)
                .build();

        mockSameDepartment();
        when(attendanceRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(timeRecordRepository.findById(10L)).thenReturn(Optional.of(timeRecord));
        when(attendanceRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timeRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approveRequest(99L, 1L);

        assertThat(result.status()).isEqualTo(ApprovalStatus.APPROVED);
        verify(timeRecordRepository).save(any());
    }

    @Test
    @DisplayName("承認するとTimeRecordが新規作成される(ADD)")
    void approveRequest_add_createsNewTimeRecord() {
        var request = AttendanceRequest.builder()
                .id(2L).employeeId(1L)
                .requestType(AttendanceRequestType.ADD)
                .workDate(LocalDate.of(2026, 7, 14))
                .requestedClockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .requestedClockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .reason("追加").status(ApprovalStatus.PENDING).version(0L)
                .build();

        mockSameDepartment();
        when(attendanceRequestRepository.findById(2L)).thenReturn(Optional.of(request));
        when(attendanceRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(timeRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.approveRequest(99L, 2L);

        assertThat(result.status()).isEqualTo(ApprovalStatus.APPROVED);
        verify(timeRecordRepository).save(any());
    }

    @Test
    @DisplayName("却下するとステータスがREJECTEDになる")
    void rejectRequest_setsRejected() {
        var request = AttendanceRequest.builder()
                .id(1L).employeeId(1L)
                .requestType(AttendanceRequestType.MODIFY)
                .workDate(LocalDate.of(2026, 7, 14))
                .requestedClockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .reason("修正").status(ApprovalStatus.PENDING).version(0L)
                .build();

        mockSameDepartment();
        when(attendanceRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(attendanceRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.rejectRequest(99L, 1L);

        assertThat(result.status()).isEqualTo(ApprovalStatus.REJECTED);
    }

    @Test
    @DisplayName("処理済み申請を再操作すると409エラー")
    void approveRequest_alreadyProcessed_throwsConflict() {
        var request = AttendanceRequest.builder()
                .id(1L).employeeId(1L)
                .requestType(AttendanceRequestType.MODIFY)
                .workDate(LocalDate.of(2026, 7, 14))
                .requestedClockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .reason("修正").status(ApprovalStatus.APPROVED).version(0L)
                .build();

        mockSameDepartment();
        when(attendanceRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> service.approveRequest(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("処理済み");
    }
}
