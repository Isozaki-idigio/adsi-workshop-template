import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: '勤怠管理システム',
  description: '社員の出退勤打刻・勤怠管理・休暇申請',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="ja">
      <body className="min-h-screen bg-gray-50">
        {children}
      </body>
    </html>
  );
}
