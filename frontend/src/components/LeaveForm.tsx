'use client';

import { useState } from 'react';
import { LeaveType } from '@/lib/types';

interface LeaveFormProps {
  onSubmit: (data: { leaveType: LeaveType; startDate: string; endDate: string; reason: string }) => Promise<void>;
  loading?: boolean;
}

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  PAID: '有給休暇',
  HALF_DAY: '半休',
  SPECIAL: '特別休暇',
};

export function LeaveForm({ onSubmit, loading }: LeaveFormProps) {
  const [leaveType, setLeaveType] = useState<LeaveType>('PAID');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [reason, setReason] = useState('');

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    await onSubmit({ leaveType, startDate, endDate: endDate || startDate, reason });
    setStartDate('');
    setEndDate('');
    setReason('');
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4 p-4 bg-white rounded-lg shadow">
      <h2 className="text-lg font-semibold">休暇申請</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div>
          <label htmlFor="leaveType" className="block text-sm font-medium text-gray-700">
            種類
          </label>
          <select
            id="leaveType"
            value={leaveType}
            onChange={(e) => setLeaveType(e.target.value as LeaveType)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          >
            {Object.entries(LEAVE_TYPE_LABELS).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="startDate" className="block text-sm font-medium text-gray-700">
            開始日
          </label>
          <input
            id="startDate"
            type="date"
            required
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
        </div>
        <div>
          <label htmlFor="endDate" className="block text-sm font-medium text-gray-700">
            終了日
          </label>
          <input
            id="endDate"
            type="date"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
        </div>
        <div>
          <label htmlFor="reason" className="block text-sm font-medium text-gray-700">
            理由
          </label>
          <input
            id="reason"
            type="text"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          />
        </div>
      </div>
      <button
        type="submit"
        disabled={loading}
        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
      >
        {loading ? '申請中...' : '申請する'}
      </button>
    </form>
  );
}
