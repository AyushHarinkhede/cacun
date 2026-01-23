export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="text-center">
          <h1 className="text-5xl font-bold text-gray-900 mb-6">
            Welcome to Cacun
          </h1>
          <p className="text-xl text-gray-600 mb-8">
            Cacun - Fresh redesign powered by MERN stack
          </p>
          <div className="space-x-4">
            <button className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-lg font-semibold transition">
              Get Started
            </button>
            <button className="bg-gray-200 hover:bg-gray-300 text-gray-900 px-8 py-3 rounded-lg font-semibold transition">
              Learn More
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-20">
          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-blue-600 mb-4">⚡</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Fast</h3>
            <p className="text-gray-600">
              Built with modern technologies for optimal performance.
            </p>
          </div>

          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-blue-600 mb-4">🔒</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Secure</h3>
            <p className="text-gray-600">
              Enterprise-grade security for your peace of mind.
            </p>
          </div>

          <div className="bg-white rounded-lg shadow-lg p-8">
            <div className="text-3xl font-bold text-blue-600 mb-4">📱</div>
            <h3 className="text-xl font-semibold text-gray-900 mb-2">
              Responsive
            </h3>
            <p className="text-gray-600">
              Seamless experience across all devices and screen sizes.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
