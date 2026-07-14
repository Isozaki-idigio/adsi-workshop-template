'use client';

import Link from 'next/link';
import { logout } from '@/lib/auth';

interface NavBarProps {
  userName?: string;
  role?: 'EMPLOYEE' | 'MANAGER';
}

export function NavBar({ userName, role }: NavBarProps) {
  return (
    <nav className="bg-white shadow-sm border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="flex items-center space-x-8">
            <Link href="/" className="text-xl font-bold text-gray-900">
              勤怠管理
            </Link>
            <div className="hidden md:flex space-x-4">
              <Link href="/" className="text-gray-600 hover:text-gray-900">
                ダッシュボード
              </Link>
              <Link href="/attendance" className="text-gray-600 hover:text-gray-900">
                勤怠
              </Link>
              <Link href="/leave" className="text-gray-600 hover:text-gray-900">
                休暇
              </Link>
              {role === 'MANAGER' && (
                <Link href="/admin" className="text-gray-600 hover:text-gray-900">
                  管理者
                </Link>
              )}
            </div>
          </div>
          <div className="flex items-center space-x-4">
            {userName && (
              <>
                <span className="text-sm text-gray-600">{userName}</span>
                <button
                  onClick={logout}
                  className="text-sm text-gray-500 hover:text-gray-700"
                >
                  ログアウト
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
