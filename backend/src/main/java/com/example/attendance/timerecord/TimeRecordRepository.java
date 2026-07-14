package com.example.attendance.timerecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TimeRecordRepository extends JpaRepository<TimeRecord, Long> {

    List<TimeRecord> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    @Query("SELECT t FROM TimeRecord t WHERE t.employeeId = :employeeId AND t.workDate = :workDate AND t.clockOut IS NULL")
    Optional<TimeRecord> findOpenRecord(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    @Query("SELECT t FROM TimeRecord t WHERE t.employeeId = :employeeId AND t.workDate BETWEEN :startDate AND :endDate ORDER BY t.workDate, t.clockIn")
    List<TimeRecord> findByEmployeeIdAndWorkDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
