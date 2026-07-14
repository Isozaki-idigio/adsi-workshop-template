package com.example.attendance.timerecord;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data/time-record-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class TimeRecordRepositoryTest {

    @Autowired
    private TimeRecordRepository repository;

    @Autowired
    private TestEntityManager em;

    private TimeRecord createRecord(Long employeeId, LocalDate date, LocalDateTime clockIn, LocalDateTime clockOut) {
        var record = TimeRecord.builder()
                .employeeId(employeeId)
                .workDate(date)
                .clockIn(clockIn)
                .clockOut(clockOut)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .version(0L)
                .build();
        return em.persistAndFlush(record);
    }

    @Test
    @DisplayName("社員IDと日付でレコードを検索できる")
    void findByEmployeeIdAndWorkDate_returnsMatchingRecords() {
        LocalDate today = LocalDate.of(2026, 7, 14);
        createRecord(101L, today, today.atTime(9, 0), today.atTime(12, 0));
        createRecord(101L, today, today.atTime(13, 0), null);
        createRecord(102L, today, today.atTime(9, 0), today.atTime(18, 0));

        List<TimeRecord> result = repository.findByEmployeeIdAndWorkDate(101L, today);

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("未退勤レコードを検索できる")
    void findOpenRecord_returnsUnclockedOutRecord() {
        LocalDate today = LocalDate.of(2026, 7, 14);
        createRecord(101L, today, today.atTime(9, 0), today.atTime(12, 0));
        createRecord(101L, today, today.atTime(13, 0), null);

        var result = repository.findOpenRecord(101L, today);

        assertThat(result).isPresent();
        assertThat(result.get().getClockOut()).isNull();
    }

    @Test
    @DisplayName("未退勤レコードがない場合はemptyを返す")
    void findOpenRecord_noOpenRecord_returnsEmpty() {
        LocalDate today = LocalDate.of(2026, 7, 14);
        createRecord(101L, today, today.atTime(9, 0), today.atTime(18, 0));

        var result = repository.findOpenRecord(101L, today);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("期間指定でレコードを検索できる")
    void findByEmployeeIdAndWorkDateBetween_returnsRecordsInRange() {
        LocalDate day1 = LocalDate.of(2026, 7, 1);
        LocalDate day2 = LocalDate.of(2026, 7, 2);
        LocalDate day3 = LocalDate.of(2026, 7, 3);
        createRecord(101L, day1, day1.atTime(9, 0), day1.atTime(18, 0));
        createRecord(101L, day2, day2.atTime(9, 0), day2.atTime(18, 0));
        createRecord(101L, day3, day3.atTime(9, 0), day3.atTime(18, 0));

        List<TimeRecord> result = repository.findByEmployeeIdAndWorkDateBetween(101L, day1, day2);

        assertThat(result).hasSize(2);
    }
}
