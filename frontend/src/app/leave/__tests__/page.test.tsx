import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import LeavePage from '../page';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

vi.mock('@/lib/auth', () => ({
  getToken: () => 'mock-token',
  fetchMe: () => Promise.resolve({ id: 1, name: 'テスト太郎', role: 'EMPLOYEE' }),
  logout: vi.fn(),
}));

vi.mock('@/lib/api-client', () => ({
  apiClient: (path: string) => {
    if (path === '/leave/balance') {
      return Promise.resolve({ totalDays: 12, usedDays: 3.5, remainingDays: 8.5 });
    }
    if (path === '/leave/requests') {
      return Promise.resolve([
        { id: 1, leaveType: 'PAID', startDate: '2026-07-10', endDate: '2026-07-10', days: 1, reason: '私用', status: 'APPROVED', createdAt: '2026-07-01' },
      ]);
    }
    return Promise.resolve({});
  },
  withBasePath: (p: string) => p,
}));

describe('LeavePage', () => {
  it('休暇申請フォームが表示される', async () => {
    render(<LeavePage />);

    expect(await screen.findByRole('heading', { level: 1, name: '休暇申請' })).toBeInTheDocument();
    expect(screen.getByLabelText('種類')).toBeInTheDocument();
    expect(screen.getByLabelText('開始日')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '申請する' })).toBeInTheDocument();
  });

  it('残日数が表示される', async () => {
    render(<LeavePage />);

    expect(await screen.findByText(/8.5日/)).toBeInTheDocument();
  });

  it('申請履歴が表示される', async () => {
    render(<LeavePage />);

    expect(await screen.findByText('承認済')).toBeInTheDocument();
  });
});
