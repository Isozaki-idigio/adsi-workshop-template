package com.example.attendance.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private TimeRecordRepository timeRecordRepository;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceServiceImpl(timeRecordRepository);
    }

    @Test
    @DisplayName("月次集計: 合計勤務時間が正しく計算される")
    void getMonthlyRecords_calculatesTotal() {
        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 17, 15))
                        .build(),
                TimeRecord.builder()
                        .id(2L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 2))
                        .clockIn(LocalDateTime.of(2026, 7, 2, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 2, 16, 15))
                        .build());

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        var result = attendanceService.getMonthlyRecords(1L, YearMonth.of(2026, 7));

        // 7/1: 8h15m = 495min, 7/2: 7h15m = 435min → total = 930min
        assertThat(result.totalWorkMinutes()).isEqualTo(930);
    }

    @Test
    @DisplayName("月次集計: 残業時間が正しく計算される（所定7h15m超過分）")
    void getMonthlyRecords_calculatesOvertime() {
        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 18, 15))
                        .build());

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        var result = attendanceService.getMonthlyRecords(1L, YearMonth.of(2026, 7));

        // 9h15m = 555min, overtime = 555 - 435 = 120min
        assertThat(result.totalOvertimeMinutes()).isEqualTo(120);
    }

    @Test
    @DisplayName("月次集計: 所定時間以下の日は残業0")
    void getMonthlyRecords_noOvertimeUnderStandard() {
        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 15, 0))
                        .build());

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        var result = attendanceService.getMonthlyRecords(1L, YearMonth.of(2026, 7));

        assertThat(result.totalOvertimeMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("月次集計: 深夜時間が正しく計算される（22:00〜05:00）")
    void getMonthlyRecords_calculatesNightMinutes() {
        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 20, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 2, 1, 0))
                        .build());

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        var result = attendanceService.getMonthlyRecords(1L, YearMonth.of(2026, 7));

        // 20:00-01:00 = 5h, night portion: 22:00-01:00 = 3h = 180min
        assertThat(result.totalNightMinutes()).isEqualTo(180);
    }

    @Test
    @DisplayName("月次集計: 中抜けの日は合算される")
    void getMonthlyRecords_sumsMultipleRecordsPerDay() {
        var records = List.of(
                TimeRecord.builder()
                        .id(1L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 12, 0))
                        .build(),
                TimeRecord.builder()
                        .id(2L).employeeId(1L)
                        .workDate(LocalDate.of(2026, 7, 1))
                        .clockIn(LocalDateTime.of(2026, 7, 1, 14, 0))
                        .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0))
                        .build());

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
                eq(1L), any(), any())).thenReturn(records);

        var result = attendanceService.getMonthlyRecords(1L, YearMonth.of(2026, 7));

        // 3h + 4h = 7h = 420min
        assertThat(result.totalWorkMinutes()).isEqualTo(420);
        assertThat(result.days()).hasSize(1);
        assertThat(result.days().get(0).totalMinutes()).isEqualTo(420);
    }
}
