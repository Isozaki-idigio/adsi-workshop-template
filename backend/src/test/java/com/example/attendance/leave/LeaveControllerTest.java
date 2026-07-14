package com.example.attendance.leave;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.attendance.auth.JwtAuthenticationFilter;
import com.example.attendance.auth.JwtTokenProvider;
import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.leave.dto.LeaveBalanceResponse;
import com.example.attendance.leave.dto.LeaveRequestResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LeaveController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaveService leaveService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockAuth() {
        when(jwtTokenProvider.validateToken("test-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("test-token")).thenReturn(1L);
        when(jwtTokenProvider.getEmployeeCode("test-token")).thenReturn("EMP001");
    }

    @Test
    @DisplayName("休暇申請が成功すると201が返る")
    void applyLeave_validRequest_returns201() throws Exception {
        mockAuth();
        var response = new LeaveRequestResponse(
                1L, LeaveType.PAID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2),
                new BigDecimal("2.0"), "旅行", ApprovalStatus.PENDING, LocalDateTime.now());
        when(leaveService.applyLeave(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/api/leave/requests")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"PAID","startDate":"2026-08-01","endDate":"2026-08-02","reason":"旅行"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.leaveType").value("PAID"))
                .andExpect(jsonPath("$.days").value(2.0));
    }

    @Test
    @DisplayName("残日数不足で400が返る")
    void applyLeave_insufficientBalance_returns400() throws Exception {
        mockAuth();
        when(leaveService.applyLeave(eq(1L), any()))
                .thenThrow(new BusinessException(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", "残日数不足"));

        mockMvc.perform(post("/api/leave/requests")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"PAID","startDate":"2026-08-01","endDate":"2026-08-05","reason":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("残日数を取得できる")
    void getBalance_returns200() throws Exception {
        mockAuth();
        var balance = new LeaveBalanceResponse(
                new BigDecimal("12.0"), new BigDecimal("3.5"), new BigDecimal("8.5"));
        when(leaveService.getBalance(1L)).thenReturn(balance);

        mockMvc.perform(get("/api/leave/balance")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDays").value(12.0))
                .andExpect(jsonPath("$.remainingDays").value(8.5));
    }

    @Test
    @DisplayName("申請一覧を取得できる")
    void getRequests_returns200() throws Exception {
        mockAuth();
        var response = new LeaveRequestResponse(
                1L, LeaveType.PAID, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 1),
                new BigDecimal("1.0"), null, ApprovalStatus.APPROVED, LocalDateTime.now());
        when(leaveService.getRequests(eq(1L), any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leave/requests")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leaveType").value("PAID"));
    }

    @Test
    @DisplayName("認証なしで401が返る")
    void applyLeave_noAuth_returns401() throws Exception {
        mockMvc.perform(post("/api/leave/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leaveType":"PAID","startDate":"2026-08-01","endDate":"2026-08-01"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
