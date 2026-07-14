'use client';

import { useState } from 'react';

interface ClockButtonProps {
  isClockedIn: boolean;
  onClockIn: () => Promise<void>;
  onClockOut: () => Promise<void>;
}

export function ClockButton({ isClockedIn, onClockIn, onClockOut }: ClockButtonProps) {
  const [loading, setLoading] = useState(false);

  async function handleClick() {
    setLoading(true);
    try {
      if (isClockedIn) {
        await onClockOut();
      } else {
        await onClockIn();
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <button
      onClick={handleClick}
      disabled={loading}
      className={`px-6 py-3 rounded-lg text-white font-medium text-lg shadow-sm disabled:opacity-50 ${
        isClockedIn
          ? 'bg-red-600 hover:bg-red-700'
          : 'bg-green-600 hover:bg-green-700'
      }`}
    >
      {loading
        ? '処理中...'
        : isClockedIn
          ? '退勤'
          : '出勤'}
    </button>
  );
}
