package com.example.attendance.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import com.example.attendance.common.enums.Role;
import com.example.attendance.department.Department;
import com.example.attendance.department.DepartmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department department;

    @BeforeEach
    void setUp() {
        department = departmentRepository.save(
                Department.builder().name("テスト部門").build());
    }

    @Test
    @DisplayName("社員を保存して取得できる")
    void save_and_findById() {
        var employee = Employee.builder()
                .employeeCode("TEST001")
                .name("テスト太郎")
                .email("test@example.com")
                .passwordHash("$2a$10$dummy")
                .departmentId(department.getId())
                .role(Role.EMPLOYEE)
                .annualLeaveDays(new BigDecimal("12.0"))
                .active(true)
                .build();

        var saved = employeeRepository.save(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(employeeRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("社員コードで検索できる")
    void findByEmployeeCode_existingCode_returnsEmployee() {
        employeeRepository.save(Employee.builder()
                .employeeCode("FIND001")
                .name("検索対象")
                .email("find@example.com")
                .passwordHash("$2a$10$dummy")
                .departmentId(department.getId())
                .build());

        var found = employeeRepository.findByEmployeeCode("FIND001");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("検索対象");
    }

    @Test
    @DisplayName("部門IDでアクティブな社員を検索できる")
    void findByDepartmentIdAndActiveTrue_returnsActiveOnly() {
        employeeRepository.save(Employee.builder()
                .employeeCode("ACTIVE01")
                .name("有効社員")
                .email("active@example.com")
                .passwordHash("$2a$10$dummy")
                .departmentId(department.getId())
                .active(true)
                .build());

        employeeRepository.save(Employee.builder()
                .employeeCode("INACTIVE01")
                .name("無効社員")
                .email("inactive@example.com")
                .passwordHash("$2a$10$dummy")
                .departmentId(department.getId())
                .active(false)
                .build());

        List<Employee> result = employeeRepository
                .findByDepartmentIdAndActiveTrue(department.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("有効社員");
    }
}
