import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { StatusBadge } from '../StatusBadge';

describe('StatusBadge', () => {
  it('PENDINGステータスで「申請中」と表示される', () => {
    render(<StatusBadge status="PENDING" />);
    expect(screen.getByText('申請中')).toBeInTheDocument();
  });

  it('APPROVEDステータスで「承認済」と表示される', () => {
    render(<StatusBadge status="APPROVED" />);
    expect(screen.getByText('承認済')).toBeInTheDocument();
  });

  it('REJECTEDステータスで「却下」と表示される', () => {
    render(<StatusBadge status="REJECTED" />);
    expect(screen.getByText('却下')).toBeInTheDocument();
  });

  it('PENDINGはyellow系のクラスを持つ', () => {
    render(<StatusBadge status="PENDING" />);
    const badge = screen.getByText('申請中');
    expect(badge.className).toContain('bg-yellow-100');
  });
});
