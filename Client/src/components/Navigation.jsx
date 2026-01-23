// Placeholder for navigation component
export default function Navigation() {
  return (
    <nav className="bg-white shadow">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <h1 className="text-2xl font-bold text-blue-600">Cacun</h1>
          </div>
          <div className="flex items-center space-x-4">
            <button className="text-gray-600 hover:text-gray-900">Home</button>
            <button className="text-gray-600 hover:text-gray-900">About</button>
            <button className="text-gray-600 hover:text-gray-900">Contact</button>
          </div>
        </div>
      </div>
    </nav>
  );
}
