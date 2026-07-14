import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import AttendancePage from '../page';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock('@/lib/auth', () => ({
  getToken: () => 'mock-token',
  fetchMe: () => Promise.resolve({ id: 1, name: 'テスト太郎', role: 'MANAGER' }),
  logout: vi.fn(),
}));

vi.mock('@/lib/api-client', () => ({
  apiClient: (path: string) => {
    if (path.startsWith('/attendance/records')) {
      return Promise.resolve({
        yearMonth: '2026-07',
        totalWorkMinutes: 930,
        totalOvertimeMinutes: 60,
        totalNightMinutes: 0,
        days: [
          {
            date: '2026-07-01',
            records: [{ id: 1, workDate: '2026-07-01', clockIn: '2026-07-01T09:00:00', clockOut: '2026-07-01T17:15:00', durationMinutes: 495 }],
            totalMinutes: 495,
            overtimeMinutes: 60,
            nightMinutes: 0,
          },
          {
            date: '2026-07-02',
            records: [{ id: 2, workDate: '2026-07-02', clockIn: '2026-07-02T09:00:00', clockOut: '2026-07-02T16:15:00', durationMinutes: 435 }],
            totalMinutes: 435,
            overtimeMinutes: 0,
            nightMinutes: 0,
          },
        ],
      });
    }
    return Promise.resolve({});
  },
  withBasePath: (p: string) => p,
}));

describe('AttendancePage', () => {
  it('月次集計サマリが表示される', async () => {
    render(<AttendancePage />);

    expect(await screen.findByText('15h30m')).toBeInTheDocument();
    expect(screen.getByText('合計勤務時間')).toBeInTheDocument();
    expect(screen.getByText('残業時間')).toBeInTheDocument();
  });

  it('日別の勤怠データが表示される', async () => {
    render(<AttendancePage />);

    expect(await screen.findByText('2026-07-01')).toBeInTheDocument();
    expect(screen.getByText('2026-07-02')).toBeInTheDocument();
  });

  it('管理者にはエクスポートボタンが表示される', async () => {
    render(<AttendancePage />);

    expect(await screen.findByText('CSV出力')).toBeInTheDocument();
    expect(screen.getByText('PDF出力')).toBeInTheDocument();
  });
});
