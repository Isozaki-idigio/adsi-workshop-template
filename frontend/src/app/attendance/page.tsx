'use client';

import { useEffect, useState } from 'react';
import { NavBar } from '@/components/NavBar';
import { ExportButton } from '@/components/ExportButton';
import { apiClient } from '@/lib/api-client';
import { getToken, fetchMe } from '@/lib/auth';
import { Employee, MonthlyAttendanceResponse } from '@/lib/types';

function formatMinutes(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h${m.toString().padStart(2, '0')}m`;
}

export default function AttendancePage() {
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [data, setData] = useState<MonthlyAttendanceResponse | null>(null);
  const [yearMonth, setYearMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${(now.getMonth() + 1).toString().padStart(2, '0')}`;
  });
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, [yearMonth]);

  async function loadData() {
    try {
      const token = getToken();
      if (!token) return;
      const [me, attendance] = await Promise.all([
        fetchMe(),
        apiClient<MonthlyAttendanceResponse>(`/attendance/records?yearMonth=${yearMonth}`, { token }),
      ]);
      setEmployee(me);
      setData(attendance);
    } catch {
      setError('データの取得に失敗しました');
    }
  }

  return (
    <>
      <NavBar userName={employee?.name} role={employee?.role} />
      <main className="max-w-7xl mx-auto px-4 py-8 space-y-6">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold text-gray-900">勤怠一覧</h1>
          <div className="flex items-center space-x-4">
            <input
              type="month"
              value={yearMonth}
              onChange={(e) => setYearMonth(e.target.value)}
              className="rounded-md border border-gray-300 px-3 py-1.5"
            />
            {employee?.role === 'MANAGER' && <ExportButton yearMonth={yearMonth} />}
          </div>
        </div>

        {error && (
          <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">{error}</div>
        )}

        {data && (
          <>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white p-4 rounded-lg shadow">
                <p className="text-sm text-gray-600">合計勤務時間</p>
                <p className="text-2xl font-bold text-blue-600">{formatMinutes(data.totalWorkMinutes)}</p>
              </div>
              <div className="bg-white p-4 rounded-lg shadow">
                <p className="text-sm text-gray-600">残業時間</p>
                <p className="text-2xl font-bold text-orange-600">{formatMinutes(data.totalOvertimeMinutes)}</p>
              </div>
              <div className="bg-white p-4 rounded-lg shadow">
                <p className="text-sm text-gray-600">深夜時間</p>
                <p className="text-2xl font-bold text-purple-600">{formatMinutes(data.totalNightMinutes)}</p>
              </div>
            </div>

            <div className="bg-white rounded-lg shadow overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日付</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">出勤</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">退勤</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">勤務</th>
                    <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">残業</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {data.days.map((day) => (
                    <tr key={day.date}>
                      <td className="px-4 py-3 text-sm">{day.date}</td>
                      <td className="px-4 py-3 text-sm">
                        {day.records.map((r) => r.clockIn?.slice(11, 16)).join(', ')}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        {day.records.map((r) => r.clockOut?.slice(11, 16) || '勤務中').join(', ')}
                      </td>
                      <td className="px-4 py-3 text-sm">{formatMinutes(day.totalMinutes)}</td>
                      <td className="px-4 py-3 text-sm text-orange-600">
                        {day.overtimeMinutes > 0 ? formatMinutes(day.overtimeMinutes) : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {data.days.length === 0 && (
                <p className="text-center text-gray-500 py-8">この月の勤怠データがありません</p>
              )}
            </div>
          </>
        )}
      </main>
    </>
  );
}
