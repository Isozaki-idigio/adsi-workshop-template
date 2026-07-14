package com.example.attendance.timerecord;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.vo.WorkDuration;
import com.example.attendance.timerecord.dto.AttendanceStatusResponse;
import com.example.attendance.timerecord.dto.DailyAttendanceResponse;
import com.example.attendance.timerecord.dto.MonthlyAttendanceResponse;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final TimeRecordRepository timeRecordRepository;

    public AttendanceServiceImpl(TimeRecordRepository timeRecordRepository) {
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public TimeRecordResponse clockIn(Long employeeId) {
        LocalDate today = LocalDate.now();
        timeRecordRepository.findOpenRecord(employeeId, today)
                .ifPresent(r -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "ALREADY_CLOCKED_IN", "既に出勤済みです");
                });

        LocalDateTime now = LocalDateTime.now();
        var record = TimeRecord.builder()
                .employeeId(employeeId)
                .workDate(today)
                .clockIn(now)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();

        var saved = timeRecordRepository.save(record);
        return toResponse(saved);
    }

    @Override
    public TimeRecordResponse clockOut(Long employeeId) {
        LocalDate today = LocalDate.now();
        var record = timeRecordRepository.findOpenRecord(employeeId, today)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "NO_OPEN_RECORD", "出勤記録がありません"));

        LocalDateTime now = LocalDateTime.now();
        record.setClockOut(now);
        record.setUpdatedAt(now);

        var saved = timeRecordRepository.save(record);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatusResponse getStatus(Long employeeId) {
        LocalDate today = LocalDate.now();
        List<TimeRecord> todayRecords = timeRecordRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        var openRecord = timeRecordRepository.findOpenRecord(employeeId, today);

        long totalMinutes = todayRecords.stream()
                .filter(r -> r.getClockOut() != null)
                .mapToLong(r -> Duration.between(r.getClockIn(), r.getClockOut()).toMinutes())
                .sum();

        return new AttendanceStatusResponse(
                openRecord.isPresent(),
                openRecord.map(this::toResponse).orElse(null),
                todayRecords.stream().map(this::toResponse).toList(),
                totalMinutes);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceResponse getRecords(Long employeeId, YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        List<TimeRecord> records = timeRecordRepository.findByEmployeeIdAndWorkDateBetween(employeeId, start, end);

        Map<LocalDate, List<TimeRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(TimeRecord::getWorkDate));

        List<DailyAttendanceResponse> dailyRecords = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toDailyResponse(entry.getKey(), entry.getValue()))
                .toList();

        long totalWork = dailyRecords.stream().mapToLong(DailyAttendanceResponse::totalMinutes).sum();
        long totalOvertime = dailyRecords.stream().mapToLong(DailyAttendanceResponse::overtimeMinutes).sum();

        return new MonthlyAttendanceResponse(yearMonth.toString(), totalWork, totalOvertime, dailyRecords);
    }

    private DailyAttendanceResponse toDailyResponse(LocalDate date, List<TimeRecord> records) {
        long totalMinutes = records.stream()
                .filter(r -> r.getClockOut() != null)
                .mapToLong(r -> Duration.between(r.getClockIn(), r.getClockOut()).toMinutes())
                .sum();

        var workDuration = new WorkDuration(Duration.ofMinutes(totalMinutes));
        boolean nightWork = records.stream()
                .anyMatch(r -> WorkDuration.isNightWork(r.getClockIn(), r.getClockOut()));

        return new DailyAttendanceResponse(
                date,
                records.stream().map(this::toResponse).toList(),
                workDuration.totalMinutes(),
                workDuration.overtimeMinutes(),
                nightWork);
    }

    private TimeRecordResponse toResponse(TimeRecord record) {
        long duration = 0;
        if (record.getClockOut() != null) {
            duration = Duration.between(record.getClockIn(), record.getClockOut()).toMinutes();
        }
        return new TimeRecordResponse(
                record.getId(),
                record.getWorkDate(),
                record.getClockIn(),
                record.getClockOut(),
                duration);
    }
}
