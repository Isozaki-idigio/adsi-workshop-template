import { apiClient } from './api-client';
import { getToken } from './auth';
import { Employee, TimeRecord } from './types';

export interface AttendanceRequestResponse {
  id: number;
  requestType: 'MODIFY' | 'ADD' | 'DELETE';
  workDate: string;
  requestedClockIn: string;
  requestedClockOut: string | null;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface LeaveRequestResponse {
  id: number;
  leaveType: 'PAID' | 'HALF_DAY' | 'SPECIAL';
  startDate: string;
  endDate: string;
  days: number;
  reason: string | null;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface PendingRequestsResponse {
  leaveRequests: LeaveRequestResponse[];
  attendanceRequests: AttendanceRequestResponse[];
}

export interface EmployeeAttendanceSummary {
  employee: Employee;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  workDays: number;
}

export interface AttendanceRequestCreateRequest {
  requestType: 'MODIFY' | 'ADD' | 'DELETE';
  timeRecordId?: number;
  workDate: string;
  requestedClockIn: string;
  requestedClockOut?: string;
  reason: string;
}

export async function getDepartmentAttendance(yearMonth: string): Promise<EmployeeAttendanceSummary[]> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<EmployeeAttendanceSummary[]>(`/admin/department/attendance?yearMonth=${yearMonth}`, { token });
}

export async function modifyEmployeeAttendance(
  employeeId: number, recordId: number, clockIn: string, clockOut?: string
): Promise<TimeRecord> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<TimeRecord>(`/admin/attendance/${employeeId}/${recordId}`, {
    method: 'PUT',
    token,
    body: JSON.stringify({ clockIn, clockOut }),
  });
}

export async function getPendingRequests(): Promise<PendingRequestsResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<PendingRequestsResponse>('/admin/pending-requests', { token });
}

export async function approveLeaveRequest(requestId: number): Promise<LeaveRequestResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<LeaveRequestResponse>(`/admin/leave-requests/${requestId}/approve`, { method: 'POST', token });
}

export async function rejectLeaveRequest(requestId: number): Promise<LeaveRequestResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<LeaveRequestResponse>(`/admin/leave-requests/${requestId}/reject`, { method: 'POST', token });
}

export async function approveAttendanceRequest(requestId: number): Promise<AttendanceRequestResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<AttendanceRequestResponse>(`/admin/attendance-requests/${requestId}/approve`, { method: 'POST', token });
}

export async function rejectAttendanceRequest(requestId: number): Promise<AttendanceRequestResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<AttendanceRequestResponse>(`/admin/attendance-requests/${requestId}/reject`, { method: 'POST', token });
}

export async function submitAttendanceRequest(request: AttendanceRequestCreateRequest): Promise<AttendanceRequestResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<AttendanceRequestResponse>('/attendance-requests', {
    method: 'POST',
    token,
    body: JSON.stringify(request),
  });
}

export async function getMyAttendanceRequests(): Promise<AttendanceRequestResponse[]> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<AttendanceRequestResponse[]>('/attendance-requests', { token });
}
