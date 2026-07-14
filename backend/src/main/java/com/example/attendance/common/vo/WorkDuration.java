package com.example.attendance.common.vo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record WorkDuration(Duration duration) {

    public static final Duration STANDARD_HOURS = Duration.ofMinutes(435); // 7h15m
    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(5, 0);

    public WorkDuration {
        if (duration == null || duration.isNegative()) {
            throw new IllegalArgumentException("duration must be non-negative");
        }
    }

    public Duration overtime() {
        if (duration.compareTo(STANDARD_HOURS) > 0) {
            return duration.minus(STANDARD_HOURS);
        }
        return Duration.ZERO;
    }

    public long totalMinutes() {
        return duration.toMinutes();
    }

    public long overtimeMinutes() {
        return overtime().toMinutes();
    }

    public static boolean isNightWork(LocalDateTime clockIn, LocalDateTime clockOut) {
        if (clockIn == null || clockOut == null) {
            return false;
        }
        LocalTime inTime = clockIn.toLocalTime();
        LocalTime outTime = clockOut.toLocalTime();
        return inTime.isAfter(NIGHT_START) || inTime.isBefore(NIGHT_END)
                || outTime.isAfter(NIGHT_START) || outTime.isBefore(NIGHT_END)
                || clockIn.toLocalDate().isBefore(clockOut.toLocalDate());
    }
}
