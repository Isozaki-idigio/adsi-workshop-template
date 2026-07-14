'use client';

import { useEffect, useState, useCallback } from 'react';
import { ClockButton } from '@/components/ClockButton';
import { TimeRecordList } from '@/components/TimeRecordList';
import { getAttendanceStatus, clockIn, clockOut, AttendanceStatusResponse } from '@/lib/attendance';

export default function DashboardPage() {
  const [status, setStatus] = useState<AttendanceStatusResponse | null>(null);
  const [error, setError] = useState('');

  const loadStatus = useCallback(async () => {
    try {
      const data = await getAttendanceStatus();
      setStatus(data);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }, []);

  useEffect(() => {
    loadStatus();
  }, [loadStatus]);

  async function handleClockIn() {
    await clockIn();
    await loadStatus();
  }

  async function handleClockOut() {
    await clockOut();
    await loadStatus();
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">ダッシュボード</h1>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">
          {error}
        </div>
      )}

      <div className="bg-white shadow rounded-lg p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-medium text-gray-900">打刻</h2>
          {status && (
            <span className={`text-sm px-2 py-1 rounded ${
              status.isClockedIn ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'
            }`}>
              {status.isClockedIn ? '勤務中' : '未出勤'}
            </span>
          )}
        </div>

        {status && (
          <ClockButton
            isClockedIn={status.isClockedIn}
            onClockIn={handleClockIn}
            onClockOut={handleClockOut}
          />
        )}
      </div>

      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-lg font-medium text-gray-900 mb-4">本日の記録</h2>
        {status && <TimeRecordList records={status.todayRecords} />}
        {status && status.todayTotalMinutes > 0 && (
          <p className="mt-3 text-sm text-gray-600">
            合計: {Math.floor(status.todayTotalMinutes / 60)}h {status.todayTotalMinutes % 60}m
          </p>
        )}
      </div>
    </div>
  );
}
