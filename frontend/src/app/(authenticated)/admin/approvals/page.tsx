'use client';

import { useEffect, useState, useCallback } from 'react';
import { ApprovalCard } from '@/components/ApprovalCard';
import {
  getPendingRequests,
  approveLeaveRequest,
  rejectLeaveRequest,
  approveAttendanceRequest,
  rejectAttendanceRequest,
  PendingRequestsResponse,
} from '@/lib/admin';

export default function ApprovalsPage() {
  const [data, setData] = useState<PendingRequestsResponse | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    try {
      const result = await getPendingRequests();
      setData(result);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  async function handleApproveLeave(id: number) {
    await approveLeaveRequest(id);
    await load();
  }

  async function handleRejectLeave(id: number) {
    await rejectLeaveRequest(id);
    await load();
  }

  async function handleApproveAttendance(id: number) {
    await approveAttendanceRequest(id);
    await load();
  }

  async function handleRejectAttendance(id: number) {
    await rejectAttendanceRequest(id);
    await load();
  }

  const totalPending = (data?.leaveRequests.length ?? 0) + (data?.attendanceRequests.length ?? 0);

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">承認管理</h1>

      {error && <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">{error}</div>}

      {totalPending === 0 && data && (
        <p className="text-gray-500">承認待ちの申請はありません</p>
      )}

      {data?.leaveRequests.map((req) => (
        <ApprovalCard
          key={`leave-${req.id}`}
          id={req.id}
          type="leave"
          title={`${req.leaveType === 'PAID' ? '有給' : req.leaveType === 'HALF_DAY' ? '半休' : '特別休暇'} (${req.days}日)`}
          description={`${req.startDate} 〜 ${req.endDate}${req.reason ? ` / ${req.reason}` : ''}`}
          onApprove={handleApproveLeave}
          onReject={handleRejectLeave}
        />
      ))}

      {data?.attendanceRequests.map((req) => (
        <ApprovalCard
          key={`att-${req.id}`}
          id={req.id}
          type="attendance"
          title={`打刻${req.requestType === 'MODIFY' ? '修正' : req.requestType === 'ADD' ? '追加' : '削除'}`}
          description={`${req.workDate} / ${req.reason}`}
          onApprove={handleApproveAttendance}
          onReject={handleRejectAttendance}
        />
      ))}
    </div>
  );
}
