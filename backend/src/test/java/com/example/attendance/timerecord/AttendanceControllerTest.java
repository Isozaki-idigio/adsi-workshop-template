package com.example.attendance.timerecord;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import com.example.attendance.auth.JwtAuthenticationFilter;
import com.example.attendance.auth.JwtTokenProvider;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.employee.EmployeeService;
import com.example.attendance.timerecord.dto.AttendanceStatusResponse;
import com.example.attendance.timerecord.dto.DailyAttendanceResponse;
import com.example.attendance.timerecord.dto.MonthlyAttendanceResponse;
import com.example.attendance.timerecord.dto.TimeRecordResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttendanceController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceService attendanceService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockAuth() {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("valid-token")).thenReturn(1L);
        when(jwtTokenProvider.getEmployeeCode("valid-token")).thenReturn("EMP001");
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in が201を返す")
    void clockIn_success_returns201() throws Exception {
        mockAuth();
        var response = new TimeRecordResponse(1L, LocalDate.now(), LocalDateTime.now(), null, 0);
        when(attendanceService.clockIn(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in 二重出勤で409を返す")
    void clockIn_alreadyClockedIn_returns409() throws Exception {
        mockAuth();
        when(attendanceService.clockIn(1L))
                .thenThrow(new BusinessException(HttpStatus.CONFLICT, "ALREADY_CLOCKED_IN", "既に出勤済みです"));

        mockMvc.perform(post("/api/attendance/clock-in")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/attendance/clock-out が200を返す")
    void clockOut_success_returns200() throws Exception {
        mockAuth();
        var response = new TimeRecordResponse(1L, LocalDate.now(),
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), 480);
        when(attendanceService.clockOut(1L)).thenReturn(response);

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(480));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-out 未出勤で404を返す")
    void clockOut_notClockedIn_returns404() throws Exception {
        mockAuth();
        when(attendanceService.clockOut(1L))
                .thenThrow(new BusinessException(HttpStatus.NOT_FOUND, "NO_OPEN_RECORD", "出勤記録がありません"));

        mockMvc.perform(post("/api/attendance/clock-out")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/attendance/status が200を返す")
    void getStatus_returns200() throws Exception {
        mockAuth();
        var statusResponse = new AttendanceStatusResponse(true, null, List.of(), 0);
        when(attendanceService.getStatus(1L)).thenReturn(statusResponse);

        mockMvc.perform(get("/api/attendance/status")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isClockedIn").value(true));
    }

    @Test
    @DisplayName("GET /api/attendance/records が200を返す")
    void getRecords_returns200() throws Exception {
        mockAuth();
        var monthly = new MonthlyAttendanceResponse("2026-07", 2400, 120, List.of());
        when(attendanceService.getRecords(eq(1L), any(YearMonth.class))).thenReturn(monthly);

        mockMvc.perform(get("/api/attendance/records")
                        .param("yearMonth", "2026-07")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-07"))
                .andExpect(jsonPath("$.totalWorkMinutes").value(2400));
    }

    @Test
    @DisplayName("認証なしで401を返す")
    void clockIn_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-in"))
                .andExpect(status().isUnauthorized());
    }
}
