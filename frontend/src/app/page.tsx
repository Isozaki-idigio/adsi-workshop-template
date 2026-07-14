import { NavBar } from '@/components/NavBar';

export default function Home() {
  return (
    <>
      <NavBar />
      <main className="max-w-7xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-900">ダッシュボード</h1>
        <p className="mt-2 text-gray-600">ログインしてください。</p>
      </main>
    </>
  );
}
