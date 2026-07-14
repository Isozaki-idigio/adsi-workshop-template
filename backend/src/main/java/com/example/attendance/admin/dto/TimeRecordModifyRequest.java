package com.example.attendance.admin.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

public record TimeRecordModifyRequest(
        @NotNull LocalDateTime clockIn,
        LocalDateTime clockOut) {
}
