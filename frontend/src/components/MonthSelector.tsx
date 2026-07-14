'use client';

interface MonthSelectorProps {
  value: string;
  onChange: (yearMonth: string) => void;
}

export function MonthSelector({ value, onChange }: MonthSelectorProps) {
  function handlePrev() {
    const [year, month] = value.split('-').map(Number);
    const date = new Date(year, month - 2, 1);
    onChange(formatYearMonth(date));
  }

  function handleNext() {
    const [year, month] = value.split('-').map(Number);
    const date = new Date(year, month, 1);
    onChange(formatYearMonth(date));
  }

  return (
    <div className="flex items-center space-x-4">
      <button
        onClick={handlePrev}
        className="px-3 py-1 border rounded hover:bg-gray-100"
        aria-label="前月"
      >
        &lt;
      </button>
      <span className="text-lg font-medium">{value}</span>
      <button
        onClick={handleNext}
        className="px-3 py-1 border rounded hover:bg-gray-100"
        aria-label="翌月"
      >
        &gt;
      </button>
    </div>
  );
}

function formatYearMonth(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  return `${y}-${m}`;
}
