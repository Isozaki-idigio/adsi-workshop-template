export type Role = 'EMPLOYEE' | 'MANAGER';

export type LeaveType = 'PAID' | 'HALF_DAY' | 'SPECIAL';

export type ApprovalStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export type AttendanceRequestType = 'MODIFY' | 'ADD' | 'DELETE';

export interface Employee {
  id: number;
  employeeCode: string;
  name: string;
  email: string;
  departmentName: string;
  role: Role;
}

export interface TimeRecord {
  id: number;
  workDate: string;
  clockIn: string;
  clockOut: string | null;
  durationMinutes: number;
}

export interface ErrorResponse {
  message: string;
  code: string;
}

export interface ValidationErrorResponse {
  message: string;
  errors: { field: string; message: string }[];
}

export interface LeaveRequestResponse {
  id: number;
  leaveType: LeaveType;
  startDate: string;
  endDate: string;
  days: number;
  reason: string | null;
  status: ApprovalStatus;
  createdAt: string;
}

export interface LeaveBalanceResponse {
  totalDays: number;
  usedDays: number;
  remainingDays: number;
}
