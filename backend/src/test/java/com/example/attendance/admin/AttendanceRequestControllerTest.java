package com.example.attendance.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.attendance.admin.dto.AttendanceRequestCreateRequest;
import com.example.attendance.admin.dto.AttendanceRequestResponse;
import com.example.attendance.auth.JwtAuthenticationFilter;
import com.example.attendance.auth.JwtTokenProvider;
import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.AttendanceRequestType;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.employee.EmployeeService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AttendanceRequestController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class AttendanceRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttendanceRequestService attendanceRequestService;

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
    @DisplayName("POST /api/attendance-requests が201を返す")
    void submitRequest_returns201() throws Exception {
        mockAuth();
        var response = new AttendanceRequestResponse(
                1L, AttendanceRequestType.MODIFY,
                LocalDate.of(2026, 7, 14),
                LocalDateTime.of(2026, 7, 14, 9, 0),
                LocalDateTime.of(2026, 7, 14, 18, 0),
                "打刻忘れ", ApprovalStatus.PENDING, LocalDateTime.now());

        when(attendanceRequestService.submitRequest(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/attendance-requests")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestType": "MODIFY",
                                  "timeRecordId": 10,
                                  "workDate": "2026-07-14",
                                  "requestedClockIn": "2026-07-14T09:00:00",
                                  "requestedClockOut": "2026-07-14T18:00:00",
                                  "reason": "打刻忘れ"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/attendance-requests が200を返す")
    void getRequests_returns200() throws Exception {
        mockAuth();
        when(attendanceRequestService.getRequests(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/attendance-requests")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("認証なしで401を返す")
    void submitRequest_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/attendance-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"requestType": "MODIFY", "workDate": "2026-07-14", "requestedClockIn": "2026-07-14T09:00:00", "reason": "test"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
