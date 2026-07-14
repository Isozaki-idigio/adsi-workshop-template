'use client';

import { useEffect, useState } from 'react';
import { NavBar } from '@/components/NavBar';
import { LeaveForm } from '@/components/LeaveForm';
import { LeaveHistory } from '@/components/LeaveHistory';
import { apiClient } from '@/lib/api-client';
import { getToken, fetchMe } from '@/lib/auth';
import { Employee, LeaveRequestResponse, LeaveBalanceResponse, LeaveType } from '@/lib/types';

export default function LeavePage() {
  const [employee, setEmployee] = useState<Employee | null>(null);
  const [balance, setBalance] = useState<LeaveBalanceResponse | null>(null);
  const [requests, setRequests] = useState<LeaveRequestResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    loadData();
  }, []);

  async function loadData() {
    try {
      const token = getToken();
      if (!token) return;
      const [me, bal, reqs] = await Promise.all([
        fetchMe(),
        apiClient<LeaveBalanceResponse>('/leave/balance', { token }),
        apiClient<LeaveRequestResponse[]>('/leave/requests', { token }),
      ]);
      setEmployee(me);
      setBalance(bal);
      setRequests(reqs);
    } catch {
      setError('データの取得に失敗しました');
    }
  }

  async function handleSubmit(data: { leaveType: LeaveType; startDate: string; endDate: string; reason: string }) {
    setError('');
    setLoading(true);
    try {
      const token = getToken();
      await apiClient('/leave/requests', {
        method: 'POST',
        token: token || undefined,
        body: JSON.stringify(data),
      });
      await loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : '申請に失敗しました');
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <NavBar userName={employee?.name} role={employee?.role} />
      <main className="max-w-7xl mx-auto px-4 py-8 space-y-6">
        <h1 className="text-2xl font-bold text-gray-900">休暇申請</h1>

        {error && (
          <div className="bg-red-50 text-red-700 p-3 rounded text-sm" role="alert">
            {error}
          </div>
        )}

        {balance && (
          <div className="bg-white p-4 rounded-lg shadow">
            <p className="text-sm text-gray-600">
              有給残日数: <span className="text-lg font-bold text-blue-600">{balance.remainingDays}日</span>
              {' '}/ {balance.totalDays}日（使用済: {balance.usedDays}日）
            </p>
          </div>
        )}

        <LeaveForm onSubmit={handleSubmit} loading={loading} />

        <div className="bg-white p-4 rounded-lg shadow">
          <h2 className="text-lg font-semibold mb-4">申請履歴</h2>
          <LeaveHistory requests={requests} />
        </div>
      </main>
    </>
  );
}
