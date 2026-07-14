package com.example.attendance.leave.dto;

import java.math.BigDecimal;

public record LeaveBalanceResponse(
        BigDecimal totalDays,
        BigDecimal usedDays,
        BigDecimal remainingDays) {
}
