import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import { ClockButton } from '../ClockButton';

describe('ClockButton', () => {
  it('未出勤時に「出勤」ボタンが表示される', () => {
    render(
      <ClockButton isClockedIn={false} onClockIn={vi.fn()} onClockOut={vi.fn()} />
    );

    expect(screen.getByRole('button', { name: '出勤' })).toBeInTheDocument();
  });

  it('勤務中に「退勤」ボタンが表示される', () => {
    render(
      <ClockButton isClockedIn={true} onClockIn={vi.fn()} onClockOut={vi.fn()} />
    );

    expect(screen.getByRole('button', { name: '退勤' })).toBeInTheDocument();
  });

  it('出勤ボタンクリックでonClockInが呼ばれる', async () => {
    const onClockIn = vi.fn().mockResolvedValue(undefined);
    render(
      <ClockButton isClockedIn={false} onClockIn={onClockIn} onClockOut={vi.fn()} />
    );

    fireEvent.click(screen.getByRole('button', { name: '出勤' }));

    await waitFor(() => {
      expect(onClockIn).toHaveBeenCalledTimes(1);
    });
  });

  it('退勤ボタンクリックでonClockOutが呼ばれる', async () => {
    const onClockOut = vi.fn().mockResolvedValue(undefined);
    render(
      <ClockButton isClockedIn={true} onClockIn={vi.fn()} onClockOut={onClockOut} />
    );

    fireEvent.click(screen.getByRole('button', { name: '退勤' }));

    await waitFor(() => {
      expect(onClockOut).toHaveBeenCalledTimes(1);
    });
  });
});
