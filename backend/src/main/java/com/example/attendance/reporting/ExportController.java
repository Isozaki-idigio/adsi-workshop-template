package com.example.attendance.reporting;

import java.time.YearMonth;

import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class ExportController {

    private final ExportService exportService;
    private final EmployeeRepository employeeRepository;

    public ExportController(ExportService exportService, EmployeeRepository employeeRepository) {
        this.exportService = exportService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAttendance(
            Authentication auth,
            @RequestParam String yearMonth,
            @RequestParam String format) {
        Long employeeId = (Long) auth.getPrincipal();
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND", "社員が見つかりません"));

        Long departmentId = employee.getDepartmentId();
        YearMonth ym = YearMonth.parse(yearMonth);

        byte[] data;
        String contentType;
        String filename;

        if ("csv".equalsIgnoreCase(format)) {
            data = exportService.exportCsv(departmentId, ym);
            contentType = "text/csv; charset=UTF-8";
            filename = "attendance_" + yearMonth + ".csv";
        } else if ("pdf".equalsIgnoreCase(format)) {
            data = exportService.exportPdf(departmentId, ym);
            contentType = "application/pdf";
            filename = "attendance_" + yearMonth + ".pdf";
        } else {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "INVALID_FORMAT", "format は csv または pdf を指定してください");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }
}
