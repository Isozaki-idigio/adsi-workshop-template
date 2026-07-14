package com.example.attendance.attendance;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findByEmployeeIdAndWorkDateBetweenOrderByClockIn(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    List<TimeRecord> findByEmployeeIdAndWorkDateOrderByClockIn(
            Long employeeId, LocalDate workDate);
}
