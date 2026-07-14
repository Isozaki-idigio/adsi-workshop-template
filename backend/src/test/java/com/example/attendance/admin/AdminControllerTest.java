package com.example.attendance.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.attendance.admin.dto.AttendanceRequestResponse;
import com.example.attendance.admin.dto.PendingRequestsResponse;
import com.example.attendance.auth.JwtAuthenticationFilter;
import com.example.attendance.auth.JwtTokenProvider;
import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.AttendanceRequestType;
import com.example.attendance.common.enums.Role;
import com.example.attendance.common.exception.GlobalExceptionHandler;
import com.example.attendance.config.SecurityConfig;
import com.example.attendance.department.DepartmentRepository;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.employee.EmployeeService;
import com.example.attendance.leave.LeaveService;
import com.example.attendance.leave.dto.LeaveRequestResponse;
import com.example.attendance.timerecord.TimeRecordRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class})
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeRepository employeeRepository;

    @MockitoBean
    private DepartmentRepository departmentRepository;

    @MockitoBean
    private TimeRecordRepository timeRecordRepository;

    @MockitoBean
    private LeaveService leaveService;

    @MockitoBean
    private AttendanceRequestService attendanceRequestService;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    private void mockManagerAuth() {
        when(jwtTokenProvider.validateToken("mgr-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("mgr-token")).thenReturn(1L);
        when(jwtTokenProvider.getEmployeeCode("mgr-token")).thenReturn("MGR001");

        var manager = Employee.builder()
                .id(1L).employeeCode("MGR001").name("山田太郎")
                .email("yamada@example.com").passwordHash("hash")
                .departmentId(1L).role(Role.MANAGER).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(manager));
    }

    private void mockNonManagerAuth() {
        when(jwtTokenProvider.validateToken("emp-token")).thenReturn(true);
        when(jwtTokenProvider.getEmployeeId("emp-token")).thenReturn(2L);
        when(jwtTokenProvider.getEmployeeCode("emp-token")).thenReturn("EMP001");

        var employee = Employee.builder()
                .id(2L).employeeCode("EMP001").name("鈴木花子")
                .email("suzuki@example.com").passwordHash("hash")
                .departmentId(1L).role(Role.EMPLOYEE).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
    }

    @Test
    @DisplayName("管理者が部門勤怠一覧を取得できる")
    void getDepartmentAttendance_manager_returns200() throws Exception {
        mockManagerAuth();
        when(employeeRepository.findByDepartmentIdAndActiveTrue(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/department/attendance")
                        .param("yearMonth", "2026-07")
                        .header("Authorization", "Bearer mgr-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("非管理者は403を返す")
    void getDepartmentAttendance_nonManager_returns403() throws Exception {
        mockNonManagerAuth();

        mockMvc.perform(get("/api/admin/department/attendance")
                        .param("yearMonth", "2026-07")
                        .header("Authorization", "Bearer emp-token"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("管理者が承認待ち一覧を取得できる")
    void getPendingRequests_manager_returns200() throws Exception {
        mockManagerAuth();
        when(employeeRepository.findByDepartmentIdAndActiveTrue(1L)).thenReturn(List.of());
        when(leaveService.getPendingByEmployeeIds(any())).thenReturn(List.of());
        when(attendanceRequestService.getPendingByEmployeeIds(any())).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/pending-requests")
                        .header("Authorization", "Bearer mgr-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leaveRequests").isArray())
                .andExpect(jsonPath("$.attendanceRequests").isArray());
    }

    @Test
    @DisplayName("管理者が休暇申請を承認できる")
    void approveLeaveRequest_manager_returns200() throws Exception {
        mockManagerAuth();
        var response = new LeaveRequestResponse(1L, null, null, null, null, null, ApprovalStatus.APPROVED, null);
        when(leaveService.approveLeave(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/admin/leave-requests/1/approve")
                        .header("Authorization", "Bearer mgr-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("管理者が打刻修正申請を承認できる")
    void approveAttendanceRequest_manager_returns200() throws Exception {
        mockManagerAuth();
        var response = new AttendanceRequestResponse(
                1L, AttendanceRequestType.MODIFY, null, null, null, "修正", ApprovalStatus.APPROVED, null);
        when(attendanceRequestService.approveRequest(1L, 1L)).thenReturn(response);

        mockMvc.perform(post("/api/admin/attendance-requests/1/approve")
                        .header("Authorization", "Bearer mgr-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @DisplayName("管理者が部下の勤怠を修正できる")
    void modifyEmployeeAttendance_manager_returns200() throws Exception {
        mockManagerAuth();
        var target = Employee.builder()
                .id(2L).employeeCode("EMP001").departmentId(1L).role(Role.EMPLOYEE).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(target));

        var record = com.example.attendance.timerecord.TimeRecord.builder()
                .id(10L).employeeId(2L)
                .workDate(java.time.LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).version(0L)
                .build();
        when(timeRecordRepository.findById(10L)).thenReturn(Optional.of(record));
        when(timeRecordRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/api/admin/attendance/2/10")
                        .header("Authorization", "Bearer mgr-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn": "2026-07-14T08:30:00", "clockOut": "2026-07-14T17:30:00"}
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("他部門の社員の勤怠修正は403")
    void modifyEmployeeAttendance_otherDepartment_returns403() throws Exception {
        mockManagerAuth();
        var target = Employee.builder()
                .id(3L).employeeCode("EMP002").departmentId(2L).role(Role.EMPLOYEE).build();
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(target));

        mockMvc.perform(put("/api/admin/attendance/3/10")
                        .header("Authorization", "Bearer mgr-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clockIn": "2026-07-14T08:30:00"}
                                """))
                .andExpect(status().isForbidden());
    }
}
