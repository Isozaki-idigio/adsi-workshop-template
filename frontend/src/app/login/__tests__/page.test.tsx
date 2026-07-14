import { render, screen } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import LoginPage from '../page';

vi.mock('next/navigation', () => ({
  useRouter: () => ({ push: vi.fn() }),
}));

describe('LoginPage', () => {
  it('ログインフォームが表示される', () => {
    render(<LoginPage />);

    expect(screen.getByText('勤怠管理システム')).toBeInTheDocument();
    expect(screen.getByLabelText('社員コード')).toBeInTheDocument();
    expect(screen.getByLabelText('パスワード')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'ログイン' })).toBeInTheDocument();
  });

  it('社員コード入力欄がrequiredである', () => {
    render(<LoginPage />);

    const input = screen.getByLabelText('社員コード');
    expect(input).toBeRequired();
  });

  it('パスワード入力欄がpasswordタイプである', () => {
    render(<LoginPage />);

    const input = screen.getByLabelText('パスワード');
    expect(input).toHaveAttribute('type', 'password');
  });
});
