import HomeFeed from '../components/HomeFeed';

export default function Home() {
  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center">
          <h1 className="text-5xl font-bold text-text mb-6">
            Welcome to Cacun
          </h1>
          <p className="text-xl text-text-muted mb-8">
            Nature-first marketplace for sustainable living
          </p>
          <div className="space-x-4">
            <button className="bg-primary hover:opacity-90 text-white px-8 py-3 rounded-lg font-semibold transition">
              Explore Products
            </button>
            <button className="bg-card hover:bg-card/80 text-text px-8 py-3 rounded-lg font-semibold transition">
              Learn More
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20">
          <div className="bg-card rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-primary mb-4">🌿</div>
            <h3 className="text-xl font-semibold text-text mb-2">Eco-Friendly</h3>
            <p className="text-text-muted">
              All products are carefully selected for their environmental impact.
            </p>
          </div>

          <div className="bg-card rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-primary mb-4">♻️</div>
            <h3 className="text-xl font-semibold text-text mb-2">Sustainable</h3>
            <p className="text-text-muted">
              Supporting brands that prioritize sustainability and ethical practices.
            </p>
          </div>

          <div className="bg-card rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-primary mb-4">🌍</div>
            <h3 className="text-xl font-semibold text-text mb-2">
              Community Driven
            </h3>
            <p className="text-text-muted">
              Join a community of conscious consumers making a difference.
            </p>
          </div>
        </div>
        
        <HomeFeed />
      </div>
    </div>
  );
}
