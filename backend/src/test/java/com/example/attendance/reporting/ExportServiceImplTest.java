package com.example.attendance.reporting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.example.attendance.attendance.TimeRecord;
import com.example.attendance.attendance.TimeRecordRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.common.enums.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private TimeRecordRepository timeRecordRepository;

    private ExportService exportService;

    @BeforeEach
    void setUp() {
        exportService = new ExportServiceImpl(employeeRepository, timeRecordRepository);
    }

    @Test
    @DisplayName("CSVにヘッダーと社員データが含まれる")
    void exportCsv_containsHeaderAndData() {
        var employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash("hash")
                .departmentId(1L)
                .role(Role.EMPLOYEE)
                .active(true)
                .build();
        when(employeeRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(List.of(employee));

        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 17, 15))
                        .build());
        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        byte[] csv = exportService.exportCsv(1L, YearMonth.of(2026, 7));
        String content = new String(csv);

        assertThat(content).contains("社員コード");
        assertThat(content).contains("EMP001");
        assertThat(content).contains("田中太郎");
        assertThat(content).contains("495"); // 8h15m = 495min
    }

    @Test
    @DisplayName("CSVのヘッダーに必要な列が含まれる")
    void exportCsv_headerHasRequiredColumns() {
        when(employeeRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(List.of());

        byte[] csv = exportService.exportCsv(1L, YearMonth.of(2026, 7));
        String content = new String(csv);

        assertThat(content).contains("社員コード");
        assertThat(content).contains("氏名");
        assertThat(content).contains("合計勤務時間(分)");
        assertThat(content).contains("残業時間(分)");
    }

    @Test
    @DisplayName("PDFが生成される（バイト配列が空でない）")
    void exportPdf_returnsNonEmptyBytes() {
        when(employeeRepository.findByDepartmentIdAndActiveTrue(1L))
                .thenReturn(List.of());

        byte[] pdf = exportService.exportPdf(1L, YearMonth.of(2026, 7));

        assertThat(pdf).isNotEmpty();
    }
}
