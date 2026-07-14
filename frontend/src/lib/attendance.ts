import { apiClient } from './api-client';
import { getToken } from './auth';
import { TimeRecord } from './types';

export interface AttendanceStatusResponse {
  isClockedIn: boolean;
  currentRecord: TimeRecord | null;
  todayRecords: TimeRecord[];
  todayTotalMinutes: number;
}

export interface DailyAttendanceResponse {
  date: string;
  records: TimeRecord[];
  totalMinutes: number;
  overtimeMinutes: number;
  isNightWork: boolean;
}

export interface MonthlyAttendanceResponse {
  yearMonth: string;
  totalWorkMinutes: number;
  totalOvertimeMinutes: number;
  records: DailyAttendanceResponse[];
}

export async function clockIn(): Promise<TimeRecord> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<TimeRecord>('/attendance/clock-in', { method: 'POST', token });
}

export async function clockOut(): Promise<TimeRecord> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<TimeRecord>('/attendance/clock-out', { method: 'POST', token });
}

export async function getAttendanceStatus(): Promise<AttendanceStatusResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<AttendanceStatusResponse>('/attendance/status', { token });
}

export async function getAttendanceRecords(yearMonth: string): Promise<MonthlyAttendanceResponse> {
  const token = getToken();
  if (!token) throw new Error('Not authenticated');
  return apiClient<MonthlyAttendanceResponse>(`/attendance/records?yearMonth=${yearMonth}`, { token });
}
