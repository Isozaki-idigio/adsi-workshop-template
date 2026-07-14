package com.example.attendance.attendance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.attendance.attendance.dto.DailyAttendanceResponse;
import com.example.attendance.attendance.dto.MonthlyAttendanceResponse;
import com.example.attendance.attendance.dto.TimeRecordResponse;

import org.springframework.stereotype.Service;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final long STANDARD_MINUTES = 435; // 7h15m
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    private final TimeRecordRepository timeRecordRepository;

    public AttendanceServiceImpl(TimeRecordRepository timeRecordRepository) {
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public MonthlyAttendanceResponse getMonthlyRecords(Long employeeId, YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TimeRecord> records = timeRecordRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByClockIn(employeeId, startDate, endDate);

        Map<LocalDate, List<TimeRecord>> byDate = records.stream()
                .collect(Collectors.groupingBy(TimeRecord::getWorkDate));

        List<DailyAttendanceResponse> days = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> buildDailyResponse(entry.getKey(), entry.getValue()))
                .toList();

        long totalWork = days.stream().mapToLong(DailyAttendanceResponse::totalMinutes).sum();
        long totalOvertime = days.stream().mapToLong(DailyAttendanceResponse::overtimeMinutes).sum();
        long totalNight = days.stream().mapToLong(DailyAttendanceResponse::nightMinutes).sum();

        return new MonthlyAttendanceResponse(
                yearMonth.toString(), totalWork, totalOvertime, totalNight, days);
    }

    private DailyAttendanceResponse buildDailyResponse(LocalDate date, List<TimeRecord> records) {
        long totalMinutes = 0;
        long nightMinutes = 0;

        List<TimeRecordResponse> recordResponses = records.stream()
                .map(this::toRecordResponse)
                .toList();

        for (TimeRecord record : records) {
            if (record.getClockOut() != null) {
                long mins = Duration.between(record.getClockIn(), record.getClockOut()).toMinutes();
                totalMinutes += mins;
                nightMinutes += calculateNightMinutes(record.getClockIn(), record.getClockOut());
            }
        }

        long overtimeMinutes = Math.max(0, totalMinutes - STANDARD_MINUTES);

        return new DailyAttendanceResponse(date, recordResponses, totalMinutes, overtimeMinutes, nightMinutes);
    }

    private long calculateNightMinutes(LocalDateTime clockIn, LocalDateTime clockOut) {
        long nightMins = 0;
        LocalDateTime current = clockIn;

        while (current.isBefore(clockOut)) {
            LocalDateTime nextMinute = current.plusMinutes(1);
            if (nextMinute.isAfter(clockOut)) {
                break;
            }
            LocalTime time = current.toLocalTime();
            if (time.equals(NIGHT_START) || time.isAfter(NIGHT_START) || time.isBefore(NIGHT_END)) {
                nightMins++;
            }
            current = nextMinute;
        }

        return nightMins;
    }

    private TimeRecordResponse toRecordResponse(TimeRecord record) {
        long durationMinutes = 0;
        if (record.getClockOut() != null) {
            durationMinutes = Duration.between(record.getClockIn(), record.getClockOut()).toMinutes();
        }
        return new TimeRecordResponse(
                record.getId(),
                record.getWorkDate().toString(),
                record.getClockIn(),
                record.getClockOut(),
                durationMinutes);
    }
}
