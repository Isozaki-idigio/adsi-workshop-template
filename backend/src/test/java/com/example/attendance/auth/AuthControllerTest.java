package com.example.attendance.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.attendance.common.dto.EmployeeResponse;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.employee.EmployeeService;

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

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("正しい認証情報でログインするとトークンが返る")
    void login_validCredentials_returnsToken() throws Exception {
        var employee = new EmployeeResponse(1L, "EMP001", "田中太郎", "tanaka@example.com", "開発部", Role.EMPLOYEE);
        when(employeeService.authenticate("EMP001", "password123")).thenReturn(employee);
        when(jwtTokenProvider.generateToken(1L, "EMP001", "EMPLOYEE")).thenReturn("jwt-token-here");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode": "EMP001", "password": "password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-here"))
                .andExpect(jsonPath("$.employee.name").value("田中太郎"));
    }

    @Test
    @DisplayName("不正な認証情報で401が返る")
    void login_invalidCredentials_returns401() throws Exception {
        when(employeeService.authenticate(any(), any()))
                .thenThrow(new BusinessException(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "認証失敗"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode": "EMP001", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("バリデーションエラーで400が返る")
    void login_emptyFields_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"employeeCode": "", "password": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("JWT付きリクエストで/api/auth/meが200を返す")
    void me_validToken_returnsEmployee() throws Exception {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("valid-token")).thenReturn(1L);
        when(jwtTokenProvider.getEmployeeCode("valid-token")).thenReturn("EMP001");
        var employee = new EmployeeResponse(1L, "EMP001", "田中太郎", "tanaka@example.com", "開発部", Role.EMPLOYEE);
        when(employeeService.getById(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("田中太郎"));
    }

    @Test
    @DisplayName("トークンなしで/api/auth/meが401を返す")
    void me_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
