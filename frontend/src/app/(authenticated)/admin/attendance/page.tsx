'use client';

import { useEffect, useState, useCallback } from 'react';
import { MonthSelector } from '@/components/MonthSelector';
import { getDepartmentAttendance, EmployeeAttendanceSummary } from '@/lib/admin';

function getCurrentYearMonth(): string {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
}

function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

export default function AdminAttendancePage() {
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [data, setData] = useState<EmployeeAttendanceSummary[]>([]);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const result = await getDepartmentAttendance(yearMonth);
      setData(result);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }, [yearMonth]);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">部門勤怠一覧</h1>
        <MonthSelector value={yearMonth} onChange={setYearMonth} />
      </div>

      {error && <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">{error}</div>}

      <div className="bg-white shadow rounded-lg overflow-x-auto">
        <table className="min-w-full text-sm">
          <thead>
            <tr className="border-b text-left text-gray-600 bg-gray-50">
              <th className="py-3 px-4">社員</th>
              <th className="py-3 px-4">出勤日数</th>
              <th className="py-3 px-4">合計勤務</th>
              <th className="py-3 px-4">残業</th>
            </tr>
          </thead>
          <tbody>
            {data.map((item) => (
              <tr key={item.employee.id} className="border-b">
                <td className="py-3 px-4">{item.employee.name}</td>
                <td className="py-3 px-4">{item.workDays}日</td>
                <td className="py-3 px-4">{formatDuration(item.totalWorkMinutes)}</td>
                <td className="py-3 px-4">{formatDuration(item.totalOvertimeMinutes)}</td>
              </tr>
            ))}
            {data.length === 0 && (
              <tr><td colSpan={4} className="py-4 px-4 text-center text-gray-500">データがありません</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
