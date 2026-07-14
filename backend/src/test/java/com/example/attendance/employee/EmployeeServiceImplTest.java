package com.example.attendance.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.example.attendance.common.dto.EmployeeResponse;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.department.Department;
import com.example.attendance.department.DepartmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    private EmployeeService employeeService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(
                employeeRepository, departmentRepository, passwordEncoder);
    }

    @Test
    @DisplayName("正しい認証情報でEmployeeResponseが返される")
    void authenticate_validCredentials_returnsResponse() {
        var employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .departmentId(1L)
                .role(Role.EMPLOYEE)
                .build();
        var department = Department.builder().id(1L).name("開発部").build();

        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        EmployeeResponse result = employeeService.authenticate("EMP001", "password123");

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.departmentName()).isEqualTo("開発部");
    }

    @Test
    @DisplayName("存在しない社員コードでBusinessExceptionが投げられる")
    void authenticate_unknownCode_throwsException() {
        when(employeeRepository.findByEmployeeCode("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.authenticate("UNKNOWN", "password"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("パスワードが不正でBusinessExceptionが投げられる")
    void authenticate_wrongPassword_throwsException() {
        var employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash(passwordEncoder.encode("correct-password"))
                .departmentId(1L)
                .role(Role.EMPLOYEE)
                .build();

        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.authenticate("EMP001", "wrong-password"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("IDで社員情報を取得できる")
    void getById_existingId_returnsResponse() {
        var employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash("hash")
                .departmentId(1L)
                .role(Role.MANAGER)
                .build();
        var department = Department.builder().id(1L).name("開発部").build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));

        EmployeeResponse result = employeeService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.role()).isEqualTo(Role.MANAGER);
    }

    @Test
    @DisplayName("存在しないIDでBusinessExceptionが投げられる")
    void getById_unknownId_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(999L))
                .isInstanceOf(BusinessException.class);
    }
}
