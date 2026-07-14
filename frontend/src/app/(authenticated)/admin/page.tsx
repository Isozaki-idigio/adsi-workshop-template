'use client';

import Link from 'next/link';

export default function AdminPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">管理者メニュー</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Link
          href="/admin/attendance"
          className="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"
        >
          <h2 className="text-lg font-medium text-gray-900">部門勤怠一覧</h2>
          <p className="text-sm text-gray-600 mt-1">部門メンバーの勤怠状況を確認・修正</p>
        </Link>

        <Link
          href="/admin/approvals"
          className="block p-6 bg-white rounded-lg shadow hover:shadow-md transition-shadow"
        >
          <h2 className="text-lg font-medium text-gray-900">承認管理</h2>
          <p className="text-sm text-gray-600 mt-1">休暇申請・打刻修正申請の承認/却下</p>
        </Link>
      </div>
    </div>
  );
}
