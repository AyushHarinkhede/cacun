import BottomNav from './BottomNav';

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-background text-text relative">
      {/* Floating Cacun Logo */}
      <header className="pointer-events-none fixed top-4 left-1/2 transform -translate-x-1/2 z-40">
        <img 
          src="/cacun.png" 
          alt="Cacun" 
          className="h-14 w-auto opacity-95 transition-opacity drop-shadow-[0_10px_24px_rgba(0,0,0,0.35)]"
        />
      </header>

      {/* Main Content */}
      <main className="pt-20 pb-28">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {children}
        </div>
      </main>

      {/* Bottom Navigation */}
      <BottomNav />
    </div>
  );
}
