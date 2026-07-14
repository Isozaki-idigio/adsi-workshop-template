package com.example.attendance.reporting;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.YearMonth;
import java.util.List;

import com.example.attendance.timerecord.TimeRecord;
import com.example.attendance.timerecord.TimeRecordRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import org.springframework.stereotype.Service;

@Service
public class ExportServiceImpl implements ExportService {

    private static final long STANDARD_MINUTES = 435;

    private final EmployeeRepository employeeRepository;
    private final TimeRecordRepository timeRecordRepository;

    public ExportServiceImpl(
            EmployeeRepository employeeRepository,
            TimeRecordRepository timeRecordRepository) {
        this.employeeRepository = employeeRepository;
        this.timeRecordRepository = timeRecordRepository;
    }

    @Override
    public byte[] exportCsv(Long departmentId, YearMonth yearMonth) {
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActiveTrue(departmentId);

        var out = new ByteArrayOutputStream();
        var writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("社員コード,氏名,合計勤務時間(分),残業時間(分)");

        for (Employee emp : employees) {
            var summary = calculateSummary(emp.getId(), yearMonth);
            writer.printf("%s,%s,%d,%d%n",
                    emp.getEmployeeCode(),
                    emp.getName(),
                    summary.totalMinutes,
                    summary.overtimeMinutes);
        }

        writer.flush();
        return out.toByteArray();
    }

    @Override
    public byte[] exportPdf(Long departmentId, YearMonth yearMonth) {
        List<Employee> employees = employeeRepository.findByDepartmentIdAndActiveTrue(departmentId);

        var out = new ByteArrayOutputStream();
        var writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("=== 月次勤怠レポート ===");
        writer.printf("対象期間: %s%n", yearMonth);
        writer.println();
        writer.printf("%-10s %-15s %10s %10s%n", "社員コード", "氏名", "勤務(分)", "残業(分)");
        writer.println("-".repeat(50));

        for (Employee emp : employees) {
            var summary = calculateSummary(emp.getId(), yearMonth);
            writer.printf("%-10s %-15s %10d %10d%n",
                    emp.getEmployeeCode(),
                    emp.getName(),
                    summary.totalMinutes,
                    summary.overtimeMinutes);
        }

        writer.flush();
        return out.toByteArray();
    }

    private EmployeeSummary calculateSummary(Long employeeId, YearMonth yearMonth) {
        var startDate = yearMonth.atDay(1);
        var endDate = yearMonth.atEndOfMonth();
        List<TimeRecord> records = timeRecordRepository
                .findByEmployeeIdAndWorkDateBetweenOrderByClockIn(employeeId, startDate, endDate);

        long totalMinutes = 0;
        var byDate = records.stream()
                .collect(java.util.stream.Collectors.groupingBy(TimeRecord::getWorkDate));

        long totalOvertime = 0;
        for (var dayRecords : byDate.values()) {
            long dayMinutes = 0;
            for (TimeRecord r : dayRecords) {
                if (r.getClockOut() != null) {
                    dayMinutes += Duration.between(r.getClockIn(), r.getClockOut()).toMinutes();
                }
            }
            totalMinutes += dayMinutes;
            totalOvertime += Math.max(0, dayMinutes - STANDARD_MINUTES);
        }

        return new EmployeeSummary(totalMinutes, totalOvertime);
    }

    private record EmployeeSummary(long totalMinutes, long overtimeMinutes) {}
}
