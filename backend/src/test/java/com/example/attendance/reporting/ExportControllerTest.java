package com.example.attendance.reporting;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import com.example.attendance.auth.JwtAuthenticationFilter;
import com.example.attendance.auth.JwtTokenProvider;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExportController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockAuth() {
        when(jwtTokenProvider.validateToken("test-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("test-token")).thenReturn(1L);
        when(jwtTokenProvider.getEmployeeCode("test-token")).thenReturn("MGR001");
        when(jwtTokenProvider.getRole("test-token")).thenReturn("MANAGER");
    }

    @Test
    @DisplayName("CSV エクスポートが200と正しいContent-Typeを返す")
    void export_csv_returns200() throws Exception {
        mockAuth();
        var employee = Employee.builder()
                .id(1L).departmentId(1L).role(Role.MANAGER).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(exportService.exportCsv(eq(1L), any())).thenReturn("csv-data".getBytes());

        mockMvc.perform(get("/api/admin/export")
                        .header("Authorization", "Bearer test-token")
                        .param("yearMonth", "2026-07")
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv;charset=UTF-8"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=attendance_2026-07.csv"));
    }

    @Test
    @DisplayName("PDF エクスポートが200を返す")
    void export_pdf_returns200() throws Exception {
        mockAuth();
        var employee = Employee.builder()
                .id(1L).departmentId(1L).role(Role.MANAGER).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(exportService.exportPdf(eq(1L), any())).thenReturn("pdf-data".getBytes());

        mockMvc.perform(get("/api/admin/export")
                        .header("Authorization", "Bearer test-token")
                        .param("yearMonth", "2026-07")
                        .param("format", "pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @DisplayName("不正なformatで400が返る")
    void export_invalidFormat_returns400() throws Exception {
        mockAuth();
        var employee = Employee.builder()
                .id(1L).departmentId(1L).role(Role.MANAGER).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        mockMvc.perform(get("/api/admin/export")
                        .header("Authorization", "Bearer test-token")
                        .param("yearMonth", "2026-07")
                        .param("format", "xlsx"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("認証なしで401が返る")
    void export_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/export")
                        .param("yearMonth", "2026-07")
                        .param("format", "csv"))
                .andExpect(status().isUnauthorized());
    }
}
