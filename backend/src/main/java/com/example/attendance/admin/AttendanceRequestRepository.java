package com.example.attendance.admin;

import java.util.List;

import com.example.attendance.common.enums.ApprovalStatus;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRequestRepository extends JpaRepository<AttendanceRequest, Long> {

    List<AttendanceRequest> findByEmployeeId(Long employeeId);

    List<AttendanceRequest> findByEmployeeIdInAndStatus(List<Long> employeeIds, ApprovalStatus status);
}
