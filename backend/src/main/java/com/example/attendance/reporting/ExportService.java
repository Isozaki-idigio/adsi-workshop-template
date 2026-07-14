package com.example.attendance.reporting;

import java.time.YearMonth;

public interface ExportService {

    byte[] exportCsv(Long departmentId, YearMonth yearMonth);

    byte[] exportPdf(Long departmentId, YearMonth yearMonth);
}
