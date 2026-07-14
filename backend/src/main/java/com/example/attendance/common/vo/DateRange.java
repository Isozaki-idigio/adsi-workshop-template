package com.example.attendance.common.vo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record DateRange(LocalDate start, LocalDate end) {

    public DateRange {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be before or equal to end");
        }
    }

    public long days() {
        return ChronoUnit.DAYS.between(start, end) + 1;
    }
}
