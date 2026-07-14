import { LeaveRequestResponse, LeaveType } from '@/lib/types';
import { StatusBadge } from './StatusBadge';

interface LeaveHistoryProps {
  requests: LeaveRequestResponse[];
}

const LEAVE_TYPE_LABELS: Record<LeaveType, string> = {
  PAID: '有給',
  HALF_DAY: '半休',
  SPECIAL: '特別',
};

export function LeaveHistory({ requests }: LeaveHistoryProps) {
  if (requests.length === 0) {
    return <p className="text-gray-500 text-sm">申請履歴がありません</p>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full divide-y divide-gray-200">
        <thead className="bg-gray-50">
          <tr>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">種類</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">期間</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">日数</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">理由</th>
            <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ステータス</th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {requests.map((req) => (
            <tr key={req.id}>
              <td className="px-4 py-3 text-sm">{LEAVE_TYPE_LABELS[req.leaveType]}</td>
              <td className="px-4 py-3 text-sm">
                {req.startDate === req.endDate
                  ? req.startDate
                  : `${req.startDate} 〜 ${req.endDate}`}
              </td>
              <td className="px-4 py-3 text-sm">{req.days}</td>
              <td className="px-4 py-3 text-sm text-gray-500">{req.reason || '-'}</td>
              <td className="px-4 py-3 text-sm">
                <StatusBadge status={req.status} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
