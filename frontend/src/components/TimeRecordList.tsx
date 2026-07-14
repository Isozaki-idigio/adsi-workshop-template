'use client';

import { TimeRecord } from '@/lib/types';

interface TimeRecordListProps {
  records: TimeRecord[];
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

export function TimeRecordList({ records }: TimeRecordListProps) {
  if (records.length === 0) {
    return <p className="text-gray-500 text-sm">本日の打刻記録はありません</p>;
  }

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full text-sm">
        <thead>
          <tr className="border-b text-left text-gray-600">
            <th className="py-2 px-3">出勤</th>
            <th className="py-2 px-3">退勤</th>
            <th className="py-2 px-3">時間</th>
          </tr>
        </thead>
        <tbody>
          {records.map((record) => (
            <tr key={record.id} className="border-b">
              <td className="py-2 px-3">{formatTime(record.clockIn)}</td>
              <td className="py-2 px-3">{formatTime(record.clockOut)}</td>
              <td className="py-2 px-3">
                {record.clockOut ? formatDuration(record.durationMinutes) : '勤務中'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
