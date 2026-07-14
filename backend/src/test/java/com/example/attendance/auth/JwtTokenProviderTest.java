package com.example.attendance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-algorithms",
                86400000L);
    }

    @Test
    @DisplayName("トークンを生成できる")
    void generateToken_returnsNonEmptyString() {
        String token = provider.generateToken(1L, "EMP001", "EMPLOYEE");

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("トークンからemployeeIdを取得できる")
    void getEmployeeId_validToken_returnsId() {
        String token = provider.generateToken(42L, "EMP042", "EMPLOYEE");

        Long employeeId = provider.getEmployeeId(token);

        assertThat(employeeId).isEqualTo(42L);
    }

    @Test
    @DisplayName("トークンからemployeeCodeを取得できる")
    void getEmployeeCode_validToken_returnsCode() {
        String token = provider.generateToken(1L, "MGR001", "MANAGER");

        String code = provider.getEmployeeCode(token);

        assertThat(code).isEqualTo("MGR001");
    }

    @Test
    @DisplayName("有効なトークンの検証がtrueを返す")
    void validateToken_validToken_returnsTrue() {
        String token = provider.generateToken(1L, "EMP001", "EMPLOYEE");

        assertThat(provider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("不正なトークンの検証がfalseを返す")
    void validateToken_invalidToken_returnsFalse() {
        assertThat(provider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("nullトークンの検証がfalseを返す")
    void validateToken_null_returnsFalse() {
        assertThat(provider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("期限切れトークンの検証がfalseを返す")
    void validateToken_expiredToken_returnsFalse() {
        var expiredProvider = new JwtTokenProvider(
                "test-secret-key-that-is-at-least-256-bits-long-for-hmac-sha-algorithms",
                -1000L);
        String token = expiredProvider.generateToken(1L, "EMP001", "EMPLOYEE");

        assertThat(provider.validateToken(token)).isFalse();
    }
}
