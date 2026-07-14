import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ApprovalCard } from '../ApprovalCard';

describe('ApprovalCard', () => {
  const defaultProps = {
    id: 1,
    type: 'leave' as const,
    title: '有給 (1日)',
    description: '2026-07-15 〜 2026-07-15',
    onApprove: vi.fn().mockResolvedValue(undefined),
    onReject: vi.fn().mockResolvedValue(undefined),
  };

  it('承認ボタンが表示される', () => {
    render(<ApprovalCard {...defaultProps} />);
    expect(screen.getByRole('button', { name: '承認' })).toBeInTheDocument();
  });

  it('却下ボタンが表示される', () => {
    render(<ApprovalCard {...defaultProps} />);
    expect(screen.getByRole('button', { name: '却下' })).toBeInTheDocument();
  });

  it('承認クリックでonApproveが呼ばれる', async () => {
    render(<ApprovalCard {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: '承認' }));
    await waitFor(() => {
      expect(defaultProps.onApprove).toHaveBeenCalledWith(1);
    });
  });

  it('却下クリックでonRejectが呼ばれる', async () => {
    render(<ApprovalCard {...defaultProps} />);
    fireEvent.click(screen.getByRole('button', { name: '却下' }));
    await waitFor(() => {
      expect(defaultProps.onReject).toHaveBeenCalledWith(1);
    });
  });

  it('タイプに応じたバッジが表示される', () => {
    render(<ApprovalCard {...defaultProps} type="attendance" title="打刻修正申請" description="修正理由" />);
    expect(screen.getByText('打刻修正')).toBeInTheDocument();
    expect(screen.getByText('打刻修正申請')).toBeInTheDocument();
  });
});
