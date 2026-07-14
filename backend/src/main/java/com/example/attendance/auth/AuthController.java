package com.example.attendance.auth;

import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.common.dto.EmployeeResponse;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.employee.EmployeeService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final EmployeeService employeeService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(EmployeeService employeeService, JwtTokenProvider jwtTokenProvider) {
        this.employeeService = employeeService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        EmployeeResponse employee = employeeService.authenticate(
                request.employeeCode(), request.password());

        String token = jwtTokenProvider.generateToken(employee.id(), employee.employeeCode(), employee.role().name());

        return ResponseEntity.ok(new LoginResponse(token, employee));
    }

    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || !(auth.getPrincipal() instanceof Long)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "認証が必要です");
        }

        Long employeeId = (Long) auth.getPrincipal();
        EmployeeResponse employee = employeeService.getById(employeeId);
        return ResponseEntity.ok(employee);
    }
}
