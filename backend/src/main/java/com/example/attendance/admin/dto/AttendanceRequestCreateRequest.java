package com.example.attendance.admin.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.attendance.common.enums.AttendanceRequestType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttendanceRequestCreateRequest(
        @NotNull AttendanceRequestType requestType,
        Long timeRecordId,
        @NotNull LocalDate workDate,
        @NotNull LocalDateTime requestedClockIn,
        LocalDateTime requestedClockOut,
        @NotBlank @Size(max = 500) String reason) {
}
