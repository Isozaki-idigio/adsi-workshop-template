'use client';

import { useState } from 'react';

interface ApprovalCardProps {
  id: number;
  type: 'leave' | 'attendance';
  title: string;
  description: string;
  onApprove: (id: number) => Promise<void>;
  onReject: (id: number) => Promise<void>;
}

export function ApprovalCard({ id, type, title, description, onApprove, onReject }: ApprovalCardProps) {
  const [loading, setLoading] = useState(false);

  async function handleApprove() {
    setLoading(true);
    try {
      await onApprove(id);
    } finally {
      setLoading(false);
    }
  }

  async function handleReject() {
    setLoading(true);
    try {
      await onReject(id);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="border rounded-lg p-4 bg-white shadow-sm">
      <div className="flex items-start justify-between">
        <div>
          <span className={`text-xs px-2 py-0.5 rounded ${
            type === 'leave' ? 'bg-blue-100 text-blue-700' : 'bg-orange-100 text-orange-700'
          }`}>
            {type === 'leave' ? '休暇' : '打刻修正'}
          </span>
          <h3 className="mt-1 font-medium text-gray-900">{title}</h3>
          <p className="text-sm text-gray-600 mt-1">{description}</p>
        </div>
        <div className="flex space-x-2 ml-4">
          <button
            onClick={handleApprove}
            disabled={loading}
            className="px-3 py-1 text-sm bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
          >
            承認
          </button>
          <button
            onClick={handleReject}
            disabled={loading}
            className="px-3 py-1 text-sm bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
          >
            却下
          </button>
        </div>
      </div>
    </div>
  );
}
