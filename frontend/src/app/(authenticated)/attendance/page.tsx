'use client';

import { useEffect, useState, useCallback } from 'react';
import { MonthSelector } from '@/components/MonthSelector';
import { getAttendanceRecords, MonthlyAttendanceResponse } from '@/lib/attendance';

function getCurrentYearMonth(): string {
  const now = new Date();
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, '0');
  return `${y}-${m}`;
}

function formatTime(datetime: string | null): string {
  if (!datetime) return '--:--';
  return new Date(datetime).toLocaleTimeString('ja-JP', { hour: '2-digit', minute: '2-digit' });
}

function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

export default function AttendancePage() {
  const [yearMonth, setYearMonth] = useState(getCurrentYearMonth());
  const [data, setData] = useState<MonthlyAttendanceResponse | null>(null);
  const [error, setError] = useState('');

  const loadRecords = useCallback(async () => {
    try {
      const result = await getAttendanceRecords(yearMonth);
      setData(result);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }, [yearMonth]);

  useEffect(() => {
    loadRecords();
  }, [loadRecords]);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">勤怠一覧</h1>
        <MonthSelector value={yearMonth} onChange={setYearMonth} />
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">
          {error}
        </div>
      )}

      {data && (
        <div className="bg-white shadow rounded-lg p-6">
          <div className="flex space-x-6 mb-4 text-sm text-gray-600">
            <span>合計勤務: {formatDuration(data.totalWorkMinutes)}</span>
            <span>残業: {formatDuration(data.totalOvertimeMinutes)}</span>
          </div>

          <div className="overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b text-left text-gray-600">
                  <th className="py-2 px-3">日付</th>
                  <th className="py-2 px-3">出勤</th>
                  <th className="py-2 px-3">退勤</th>
                  <th className="py-2 px-3">勤務時間</th>
                  <th className="py-2 px-3">残業</th>
                  <th className="py-2 px-3">深夜</th>
                </tr>
              </thead>
              <tbody>
                {data.records.map((day) => (
                  <tr key={day.date} className="border-b">
                    <td className="py-2 px-3">{day.date}</td>
                    <td className="py-2 px-3">
                      {day.records.length > 0 ? formatTime(day.records[0].clockIn) : '--:--'}
                    </td>
                    <td className="py-2 px-3">
                      {day.records.length > 0
                        ? formatTime(day.records[day.records.length - 1].clockOut)
                        : '--:--'}
                    </td>
                    <td className="py-2 px-3">{formatDuration(day.totalMinutes)}</td>
                    <td className="py-2 px-3">
                      {day.overtimeMinutes > 0 ? formatDuration(day.overtimeMinutes) : '-'}
                    </td>
                    <td className="py-2 px-3">
                      {day.isNightWork && (
                        <span className="text-purple-600">深夜</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
