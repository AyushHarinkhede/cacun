import BottomNav from './BottomNav';

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-background text-text">
      <main className="pb-28">
        {children}
      </main>
      <BottomNav />
    </div>
  );
}
