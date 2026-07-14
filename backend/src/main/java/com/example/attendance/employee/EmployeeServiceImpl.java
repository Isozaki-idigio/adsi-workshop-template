package com.example.attendance.employee;

import com.example.attendance.common.dto.EmployeeResponse;
import com.example.attendance.common.exception.BusinessException;
import com.example.attendance.department.DepartmentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EmployeeResponse authenticate(String employeeCode, String password) {
        var employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.UNAUTHORIZED, "AUTH_FAILED",
                        "社員コードまたはパスワードが正しくありません"));

        if (!passwordEncoder.matches(password, employee.getPasswordHash())) {
            throw new BusinessException(
                    HttpStatus.UNAUTHORIZED, "AUTH_FAILED",
                    "社員コードまたはパスワードが正しくありません");
        }

        return toResponse(employee);
    }

    @Override
    public EmployeeResponse getById(Long employeeId) {
        var employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "EMPLOYEE_NOT_FOUND",
                        "社員が見つかりません"));

        return toResponse(employee);
    }

    private EmployeeResponse toResponse(Employee employee) {
        var departmentName = departmentRepository.findById(employee.getDepartmentId())
                .map(d -> d.getName())
                .orElse("");

        return new EmployeeResponse(
                employee.getId(),
                employee.getEmployeeCode(),
                employee.getName(),
                employee.getEmail(),
                departmentName,
                employee.getRole());
    }
}
