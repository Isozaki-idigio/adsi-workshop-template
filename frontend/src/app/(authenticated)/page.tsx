'use client';

import { useEffect, useState, useCallback } from 'react';
import { ClockButton } from '@/components/ClockButton';
import { TimeRecordList } from '@/components/TimeRecordList';
import { getAttendanceStatus, clockIn, clockOut, AttendanceStatusResponse } from '@/lib/attendance';

export default function DashboardPage() {
  const [status, setStatus] = useState<AttendanceStatusResponse | null>(null);
  const [error, setError] = useState('');
  const [showEffect, setShowEffect] = useState(false);
  const [particles, setParticles] = useState<Array<{ id: number; x: number; y: number; color: string; delay: number }>>([]);

  const loadStatus = useCallback(async () => {
    try {
      const data = await getAttendanceStatus();
      setStatus(data);
      setError('');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'エラーが発生しました');
    }
  }, []);

  useEffect(() => {
    loadStatus();
  }, [loadStatus]);

  function triggerEffect() {
    setShowEffect(true);
    const newParticles = Array.from({ length: 50 }, (_, i) => ({
      id: i,
      x: Math.random() * 100,
      y: Math.random() * 100,
      color: ['#ff0000', '#ff6600', '#ffcc00', '#00ff00', '#00ccff', '#ff00ff', '#ffffff'][Math.floor(Math.random() * 7)],
      delay: Math.random() * 0.5,
    }));
    setParticles(newParticles);
    setTimeout(() => setShowEffect(false), 3000);
  }

  async function handleClockIn() {
    triggerEffect();
    await clockIn();
    await loadStatus();
  }

  async function handleClockOut() {
    triggerEffect();
    await clockOut();
    await loadStatus();
  }

  return (
    <div className="relative min-h-[80vh] overflow-hidden">
      {/* Background animated gradient */}
      <div className="absolute inset-0 -z-10 animate-pulse-slow">
        <div className="absolute inset-0 bg-gradient-to-br from-red-600 via-yellow-500 to-purple-700 opacity-20 animate-spin-slow rounded-3xl" />
        <div className="absolute inset-0 bg-gradient-to-tr from-blue-500 via-pink-500 to-orange-400 opacity-15 animate-reverse-spin rounded-3xl" />
      </div>

      {/* Particle explosion effect */}
      {showEffect && (
        <div className="fixed inset-0 z-50 pointer-events-none">
          {particles.map((p) => (
            <div
              key={p.id}
              className="absolute animate-particle"
              style={{
                left: `${p.x}%`,
                top: `${p.y}%`,
                backgroundColor: p.color,
                width: '12px',
                height: '12px',
                borderRadius: '50%',
                animationDelay: `${p.delay}s`,
                boxShadow: `0 0 20px ${p.color}, 0 0 40px ${p.color}, 0 0 60px ${p.color}`,
              }}
            />
          ))}
          <div className="absolute inset-0 flex items-center justify-center">
            <span className="text-8xl font-black animate-bounce-in text-transparent bg-clip-text bg-gradient-to-r from-yellow-300 via-red-500 to-pink-500 drop-shadow-[0_0_30px_rgba(255,0,0,0.8)]">
              激熱！！
            </span>
          </div>
        </div>
      )}

      <div className="space-y-6 relative z-10">
        {/* Title with fire effect */}
        <div className="text-center py-4">
          <h1 className="text-4xl font-black text-transparent bg-clip-text bg-gradient-to-r from-red-500 via-yellow-400 to-red-500 animate-gradient-x drop-shadow-lg">
            🔥 勤怠ダッシュボード 🔥
          </h1>
          <div className="flex justify-center gap-2 mt-2">
            {['⭐', '🌟', '💫', '✨', '🌟', '⭐'].map((emoji, i) => (
              <span key={i} className="text-2xl animate-bounce" style={{ animationDelay: `${i * 0.1}s` }}>
                {emoji}
              </span>
            ))}
          </div>
        </div>

        {error && (
          <div className="bg-red-50 text-red-700 p-3 rounded text-sm border-2 border-red-400 animate-shake" role="alert">
            {error}
          </div>
        )}

        {/* Clock in/out card - パチンコ台風 */}
        <div className="relative bg-gradient-to-br from-gray-900 via-purple-900 to-gray-900 shadow-2xl rounded-2xl p-8 border-4 border-yellow-400 overflow-hidden">
          {/* Spinning border lights */}
          <div className="absolute inset-0 rounded-2xl overflow-hidden">
            <div className="absolute -inset-1 bg-gradient-to-r from-red-500 via-yellow-500 via-green-500 via-blue-500 to-purple-500 animate-spin-slow opacity-30" />
          </div>

          {/* Corner lights */}
          <div className="absolute top-2 left-2 w-4 h-4 bg-red-500 rounded-full animate-ping" />
          <div className="absolute top-2 right-2 w-4 h-4 bg-yellow-500 rounded-full animate-ping" style={{ animationDelay: '0.2s' }} />
          <div className="absolute bottom-2 left-2 w-4 h-4 bg-green-500 rounded-full animate-ping" style={{ animationDelay: '0.4s' }} />
          <div className="absolute bottom-2 right-2 w-4 h-4 bg-blue-500 rounded-full animate-ping" style={{ animationDelay: '0.6s' }} />

          <div className="relative z-10">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-black text-yellow-300 tracking-wider drop-shadow-[0_0_10px_rgba(255,255,0,0.8)]">
                ⚡ 打 刻 ⚡
              </h2>
              {status && (
                <span className={`text-lg font-bold px-4 py-2 rounded-full border-2 animate-pulse ${
                  status.isClockedIn
                    ? 'bg-green-500/30 text-green-300 border-green-400 shadow-[0_0_20px_rgba(0,255,0,0.5)]'
                    : 'bg-red-500/30 text-red-300 border-red-400 shadow-[0_0_20px_rgba(255,0,0,0.5)]'
                }`}>
                  {status.isClockedIn ? '🟢 勤務中' : '🔴 未出勤'}
                </span>
              )}
            </div>

            {status && (
              <div className="flex justify-center gap-8">
                <button
                  onClick={handleClockIn}
                  disabled={status.isClockedIn}
                  className="relative group px-8 py-4 bg-gradient-to-br from-green-400 to-green-600 text-white font-black text-xl rounded-xl shadow-lg transform transition-all hover:scale-110 hover:shadow-[0_0_40px_rgba(0,255,0,0.7)] disabled:opacity-30 disabled:hover:scale-100 disabled:hover:shadow-none border-2 border-green-300"
                >
                  <span className="relative z-10">🌅 出勤</span>
                  <div className="absolute inset-0 bg-white/20 rounded-xl animate-pulse opacity-0 group-hover:opacity-100" />
                </button>
                <button
                  onClick={handleClockOut}
                  disabled={!status.isClockedIn}
                  className="relative group px-8 py-4 bg-gradient-to-br from-orange-400 to-red-600 text-white font-black text-xl rounded-xl shadow-lg transform transition-all hover:scale-110 hover:shadow-[0_0_40px_rgba(255,100,0,0.7)] disabled:opacity-30 disabled:hover:scale-100 disabled:hover:shadow-none border-2 border-orange-300"
                >
                  <span className="relative z-10">🌇 退勤</span>
                  <div className="absolute inset-0 bg-white/20 rounded-xl animate-pulse opacity-0 group-hover:opacity-100" />
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Today's records - ネオン風 */}
        <div className="bg-gradient-to-br from-gray-900 to-indigo-900 shadow-2xl rounded-2xl p-6 border-2 border-cyan-400 shadow-[0_0_15px_rgba(0,255,255,0.3)]">
          <h2 className="text-xl font-black text-cyan-300 mb-4 tracking-wider drop-shadow-[0_0_10px_rgba(0,255,255,0.8)]">
            📊 本日の記録
          </h2>
          {status && <TimeRecordList records={status.todayRecords} />}
          {status && status.todayTotalMinutes > 0 && (
            <div className="mt-4 p-3 bg-gradient-to-r from-purple-500/20 to-pink-500/20 rounded-xl border border-purple-400/50">
              <p className="text-lg font-bold text-center text-transparent bg-clip-text bg-gradient-to-r from-yellow-300 to-pink-300">
                🕐 合計: {Math.floor(status.todayTotalMinutes / 60)}h {status.todayTotalMinutes % 60}m
              </p>
            </div>
          )}
        </div>
      </div>

      <style jsx>{`
        @keyframes spin-slow {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
        @keyframes reverse-spin {
          from { transform: rotate(360deg); }
          to { transform: rotate(0deg); }
        }
        @keyframes gradient-x {
          0%, 100% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
        }
        @keyframes particle {
          0% { transform: scale(0) translate(0, 0); opacity: 1; }
          50% { transform: scale(2) translate(${Math.random() > 0.5 ? '' : '-'}${20 + Math.random() * 40}px, ${Math.random() > 0.5 ? '' : '-'}${20 + Math.random() * 40}px); opacity: 0.8; }
          100% { transform: scale(0) translate(${Math.random() > 0.5 ? '' : '-'}${60 + Math.random() * 100}px, ${Math.random() > 0.5 ? '' : '-'}${60 + Math.random() * 100}px); opacity: 0; }
        }
        @keyframes bounce-in {
          0% { transform: scale(0) rotate(-10deg); opacity: 0; }
          50% { transform: scale(1.3) rotate(5deg); opacity: 1; }
          100% { transform: scale(1) rotate(0deg); opacity: 1; }
        }
        @keyframes shake {
          0%, 100% { transform: translateX(0); }
          25% { transform: translateX(-5px); }
          75% { transform: translateX(5px); }
        }
        .animate-spin-slow { animation: spin-slow 8s linear infinite; }
        .animate-reverse-spin { animation: reverse-spin 6s linear infinite; }
        .animate-gradient-x { background-size: 200% 200%; animation: gradient-x 3s ease infinite; }
        .animate-particle { animation: particle 1.5s ease-out forwards; }
        .animate-bounce-in { animation: bounce-in 0.6s ease-out forwards; }
        .animate-shake { animation: shake 0.3s ease-in-out; }
        .animate-pulse-slow { animation: pulse 4s ease-in-out infinite; }
      `}</style>
    </div>
  );
}
