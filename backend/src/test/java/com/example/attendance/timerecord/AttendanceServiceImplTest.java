package com.example.attendance.timerecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import com.example.attendance.common.exception.BusinessException;

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
    @DisplayName("出勤打刻: 未退勤レコードがなければ新規作成される")
    void clockIn_noOpenRecord_createsNewRecord() {
        when(timeRecordRepository.findOpenRecord(any(), any())).thenReturn(Optional.empty());
        when(timeRecordRepository.save(any())).thenAnswer(invocation -> {
            TimeRecord saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        var result = attendanceService.clockIn(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.clockIn()).isNotNull();
        assertThat(result.clockOut()).isNull();
    }

    @Test
    @DisplayName("出勤打刻: 未退勤レコードがあれば409エラー")
    void clockIn_openRecordExists_throwsConflict() {
        var openRecord = TimeRecord.builder()
                .id(1L)
                .employeeId(1L)
                .workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(2))
                .build();
        when(timeRecordRepository.findOpenRecord(any(), any())).thenReturn(Optional.of(openRecord));

        assertThatThrownBy(() -> attendanceService.clockIn(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出勤済み");
    }

    @Test
    @DisplayName("退勤打刻: 未退勤レコードがあれば退勤時刻が記録される")
    void clockOut_openRecordExists_recordsClockOut() {
        var openRecord = TimeRecord.builder()
                .id(1L)
                .employeeId(1L)
                .workDate(LocalDate.now())
                .clockIn(LocalDateTime.now().minusHours(8))
                .createdAt(LocalDateTime.now().minusHours(8))
                .updatedAt(LocalDateTime.now().minusHours(8))
                .version(0L)
                .build();
        when(timeRecordRepository.findOpenRecord(any(), any())).thenReturn(Optional.of(openRecord));
        when(timeRecordRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = attendanceService.clockOut(1L);

        assertThat(result.clockOut()).isNotNull();
        assertThat(result.durationMinutes()).isGreaterThan(0);
    }

    @Test
    @DisplayName("退勤打刻: 未退勤レコードがなければ404エラー")
    void clockOut_noOpenRecord_throwsNotFound() {
        when(timeRecordRepository.findOpenRecord(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.clockOut(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("出勤記録がありません");
    }

    @Test
    @DisplayName("打刻状態取得: 未退勤レコードがあればisClockedIn=true")
    void getStatus_openRecordExists_returnsClockedIn() {
        LocalDate today = LocalDate.now();
        var openRecord = TimeRecord.builder()
                .id(2L)
                .employeeId(1L)
                .workDate(today)
                .clockIn(today.atTime(13, 0))
                .createdAt(today.atTime(13, 0))
                .updatedAt(today.atTime(13, 0))
                .version(0L)
                .build();
        var closedRecord = TimeRecord.builder()
                .id(1L)
                .employeeId(1L)
                .workDate(today)
                .clockIn(today.atTime(9, 0))
                .clockOut(today.atTime(12, 0))
                .createdAt(today.atTime(9, 0))
                .updatedAt(today.atTime(12, 0))
                .version(0L)
                .build();

        when(timeRecordRepository.findByEmployeeIdAndWorkDate(1L, today))
                .thenReturn(List.of(closedRecord, openRecord));
        when(timeRecordRepository.findOpenRecord(1L, today))
                .thenReturn(Optional.of(openRecord));

        var result = attendanceService.getStatus(1L);

        assertThat(result.isClockedIn()).isTrue();
        assertThat(result.currentRecord()).isNotNull();
        assertThat(result.todayRecords()).hasSize(2);
        assertThat(result.todayTotalMinutes()).isEqualTo(180);
    }

    @Test
    @DisplayName("月別勤怠一覧: 日ごとに集計される")
    void getRecords_returnsMonthlyAggregation() {
        YearMonth month = YearMonth.of(2026, 7);
        LocalDate day1 = month.atDay(1);
        LocalDate day2 = month.atDay(2);

        var record1 = TimeRecord.builder()
                .id(1L).employeeId(1L).workDate(day1)
                .clockIn(day1.atTime(9, 0)).clockOut(day1.atTime(18, 0))
                .createdAt(day1.atTime(9, 0)).updatedAt(day1.atTime(18, 0)).version(0L)
                .build();
        var record2 = TimeRecord.builder()
                .id(2L).employeeId(1L).workDate(day2)
                .clockIn(day2.atTime(9, 0)).clockOut(day2.atTime(17, 0))
                .createdAt(day2.atTime(9, 0)).updatedAt(day2.atTime(17, 0)).version(0L)
                .build();

        when(timeRecordRepository.findByEmployeeIdAndWorkDateBetween(1L, month.atDay(1), month.atEndOfMonth()))
                .thenReturn(List.of(record1, record2));

        var result = attendanceService.getRecords(1L, month);

        assertThat(result.yearMonth()).isEqualTo("2026-07");
        assertThat(result.records()).hasSize(2);
        assertThat(result.totalWorkMinutes()).isEqualTo(540 + 480);
    }
}
