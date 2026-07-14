'use client';

import { useState, useEffect, useCallback } from 'react';
import { submitAttendanceRequest, getMyAttendanceRequests, AttendanceRequestResponse } from '@/lib/admin';

export default function AttendanceRequestPage() {
  const [requests, setRequests] = useState<AttendanceRequestResponse[]>([]);
  const [requestType, setRequestType] = useState<'MODIFY' | 'ADD' | 'DELETE'>('MODIFY');
  const [workDate, setWorkDate] = useState('');
  const [clockIn, setClockIn] = useState('');
  const [clockOut, setClockOut] = useState('');
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadRequests = useCallback(async () => {
    try {
      const data = await getMyAttendanceRequests();
      setRequests(data);
    } catch (err) {
      // ignore load errors
    }
  }, []);

  useEffect(() => { loadRequests(); }, [loadRequests]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');

    try {
      await submitAttendanceRequest({
        requestType,
        workDate,
        requestedClockIn: `${workDate}T${clockIn}:00`,
        requestedClockOut: clockOut ? `${workDate}T${clockOut}:00` : undefined,
        reason,
      });
      setSuccess('申請を送信しました');
      setWorkDate('');
      setClockIn('');
      setClockOut('');
      setReason('');
      await loadRequests();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">打刻修正申請</h1>

      <div className="bg-white shadow rounded-lg p-6">
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">{error}</div>}
          {success && <div className="bg-green-50 text-green-700 p-3 rounded text-sm">{success}</div>}

          <div>
            <label className="block text-sm font-medium text-gray-700">申請種類</label>
            <select
              value={requestType}
              onChange={(e) => setRequestType(e.target.value as 'MODIFY' | 'ADD' | 'DELETE')}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
            >
              <option value="MODIFY">修正</option>
              <option value="ADD">追加</option>
              <option value="DELETE">削除</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">対象日</label>
            <input type="date" required value={workDate} onChange={(e) => setWorkDate(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">出勤時刻</label>
              <input type="time" required value={clockIn} onChange={(e) => setClockIn(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">退勤時刻</label>
              <input type="time" value={clockOut} onChange={(e) => setClockOut(e.target.value)}
                className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2" />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700">理由</label>
            <textarea required value={reason} onChange={(e) => setReason(e.target.value)}
              className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2" rows={3} />
          </div>

          <button type="submit"
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
            申請する
          </button>
        </form>
      </div>

      <div className="bg-white shadow rounded-lg p-6">
        <h2 className="text-lg font-medium text-gray-900 mb-4">申請履歴</h2>
        {requests.length === 0 ? (
          <p className="text-gray-500 text-sm">申請履歴はありません</p>
        ) : (
          <table className="min-w-full text-sm">
            <thead>
              <tr className="border-b text-left text-gray-600">
                <th className="py-2 px-3">日付</th>
                <th className="py-2 px-3">種類</th>
                <th className="py-2 px-3">理由</th>
                <th className="py-2 px-3">ステータス</th>
              </tr>
            </thead>
            <tbody>
              {requests.map((req) => (
                <tr key={req.id} className="border-b">
                  <td className="py-2 px-3">{req.workDate}</td>
                  <td className="py-2 px-3">{req.requestType}</td>
                  <td className="py-2 px-3">{req.reason}</td>
                  <td className="py-2 px-3">
                    <span className={`px-2 py-0.5 rounded text-xs ${
                      req.status === 'APPROVED' ? 'bg-green-100 text-green-700' :
                      req.status === 'REJECTED' ? 'bg-red-100 text-red-700' :
                      'bg-yellow-100 text-yellow-700'
                    }`}>{req.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
