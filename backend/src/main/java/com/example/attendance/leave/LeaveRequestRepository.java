package com.example.attendance.leave;

import java.math.BigDecimal;
import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;
import com.example.attendance.common.enums.LeaveType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, ApprovalStatus status);

    @Query("SELECT COALESCE(SUM(lr.days), 0) FROM LeaveRequest lr " +
            "WHERE lr.employeeId = :employeeId " +
            "AND lr.status = 'APPROVED' " +
            "AND lr.leaveType IN (:types)")
    BigDecimal sumApprovedDaysByEmployeeIdAndLeaveTypes(
            @Param("employeeId") Long employeeId,
            @Param("types") List<LeaveType> types);

    List<LeaveRequest> findByEmployeeIdInAndStatus(List<Long> employeeIds, ApprovalStatus status);
}
