import BottomNav from './BottomNav';

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-background text-text">
      {/* Floating logo: centered on mobile, left on larger screens */}
      <header className="pointer-events-none">
        <div className="fixed top-4 left-1/2 transform -translate-x-1/2 sm:left-6 sm:translate-x-0 z-40">
          <img src="/cacun.png" alt="Cacun" className="h-12 w-auto opacity-90" />
        </div>
      </header>

      <main className="pt-20 pb-28">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">{children}</div>
      </main>

      <BottomNav />
    </div>
  );
}
