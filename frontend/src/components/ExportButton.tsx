'use client';

import { getToken } from '@/lib/auth';
import { withBasePath } from '@/lib/api-client';

interface ExportButtonProps {
  yearMonth: string;
}

export function ExportButton({ yearMonth }: ExportButtonProps) {
  async function handleExport(format: 'csv' | 'pdf') {
    const token = getToken();
    if (!token) return;

    const url = withBasePath(`/api/admin/export?yearMonth=${yearMonth}&format=${format}`);
    const response = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    });

    if (!response.ok) return;

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = `attendance_${yearMonth}.${format}`;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  }

  return (
    <div className="flex space-x-2">
      <button
        onClick={() => handleExport('csv')}
        className="px-3 py-1.5 text-sm bg-green-600 text-white rounded hover:bg-green-700"
      >
        CSV出力
      </button>
      <button
        onClick={() => handleExport('pdf')}
        className="px-3 py-1.5 text-sm bg-purple-600 text-white rounded hover:bg-purple-700"
      >
        PDF出力
      </button>
    </div>
  );
}
