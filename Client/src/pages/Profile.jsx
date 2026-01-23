import { useState } from 'react';

export default function Profile() {
  const [activeTab, setActiveTab] = useState('orders');

  const tabs = [
    { id: 'orders', label: 'My Orders', icon: '📦' },
    { id: 'wishlist', label: 'Wishlist', icon: '❤️' },
    { id: 'impact', label: 'Eco Impact', icon: '🌍' },
    { id: 'settings', label: 'Settings', icon: '⚙️' },
  ];

  const mockOrders = [
    {
      id: 'ORD001',
      date: '2024-01-15',
      total: 67.97,
      status: 'Delivered',
      items: 3,
      ecoSaved: '2.5kg CO2',
    },
    {
      id: 'ORD002',
      date: '2024-01-10',
      total: 45.99,
      status: 'Shipped',
      items: 2,
      ecoSaved: '1.8kg CO2',
    },
  ];

  const mockWishlist = [
    {
      id: 1,
      name: 'Solar Power Bank',
      seller: 'SunEnergy',
      price: 45.99,
      ecoScore: 92,
    },
    {
      id: 2,
      name: 'Organic Cotton T-Shirt',
      seller: 'EcoWear',
      price: 29.99,
      ecoScore: 90,
    },
  ];

  const renderContent = () => {
    switch (activeTab) {
      case 'orders':
        return (
          <div className="space-y-4">
            {mockOrders.map((order) => (
              <div key={order.id} className="bg-card rounded-lg p-6">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-text">Order {order.id}</h3>
                    <p className="text-text-muted">{order.date}</p>
                    <p className="text-sm text-text-muted mt-1">{order.items} items • ${order.total}</p>
                    <div className="flex items-center mt-2">
                      <span className="text-green-500 text-sm">🌿 {order.ecoSaved} saved</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                      order.status === 'Delivered' 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-blue-100 text-blue-800'
                    }`}>
                      {order.status}
                    </span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        );

      case 'wishlist':
        return (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {mockWishlist.map((item) => (
              <div key={item.id} className="bg-card rounded-lg p-6">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold text-text">{item.name}</h3>
                    <p className="text-text-muted">{item.seller}</p>
                    <p className="text-primary font-bold mt-2">${item.price}</p>
                    <div className="flex items-center mt-2">
                      <span className="text-green-500 text-sm">🌿 {item.ecoScore}% eco score</span>
                    </div>
                  </div>
                  <button className="px-4 py-2 bg-primary text-white rounded-lg hover:opacity-90 transition">
                    Add to Cart
                  </button>
                </div>
              </div>
            ))}
          </div>
        );

      case 'impact':
        return (
          <div className="bg-card rounded-lg p-8">
            <h2 className="text-2xl font-bold text-text mb-6">Your Environmental Impact</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
              <div className="text-center">
                <div className="text-4xl font-bold text-green-500">47.3kg</div>
                <p className="text-text-muted mt-2">CO2 Saved</p>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-blue-500">23</div>
                <p className="text-text-muted mt-2">Eco Products</p>
              </div>
              <div className="text-center">
                <div className="text-4xl font-bold text-purple-500">156</div>
                <p className="text-text-muted mt-2">Plastic Items Avoided</p>
              </div>
            </div>
            <div className="mt-8">
              <h3 className="text-lg font-semibold text-text mb-4">Monthly Progress</h3>
              <div className="space-y-3">
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-text-muted">CO2 Reduction</span>
                    <span className="text-text">85%</span>
                  </div>
                  <div className="w-full bg-gray-700 rounded-full h-2">
                    <div className="bg-green-500 h-2 rounded-full" style={{ width: '85%' }}></div>
                  </div>
                </div>
                <div>
                  <div className="flex justify-between text-sm mb-1">
                    <span className="text-text-muted">Sustainable Choices</span>
                    <span className="text-text">92%</span>
                  </div>
                  <div className="w-full bg-gray-700 rounded-full h-2">
                    <div className="bg-blue-500 h-2 rounded-full" style={{ width: '92%' }}></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        );

      case 'settings':
        return (
          <div className="bg-card rounded-lg p-8">
            <h2 className="text-2xl font-bold text-text mb-6">Account Settings</h2>
            <div className="space-y-6">
              <div>
                <label className="block text-text mb-2">Full Name</label>
                <input
                  type="text"
                  defaultValue="John Doe"
                  className="w-full px-4 py-2 bg-background text-text rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-text mb-2">Email</label>
                <input
                  type="email"
                  defaultValue="john@example.com"
                  className="w-full px-4 py-2 bg-background text-text rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
                />
              </div>
              <div>
                <label className="block text-text mb-2">Notifications</label>
                <div className="space-y-2">
                  <label className="flex items-center">
                    <input type="checkbox" defaultChecked className="mr-2" />
                    <span className="text-text-muted">Email notifications for orders</span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" defaultChecked className="mr-2" />
                    <span className="text-text-muted">New eco products alerts</span>
                  </label>
                </div>
              </div>
              <button className="px-6 py-2 bg-primary text-white rounded-lg hover:opacity-90 transition">
                Save Changes
              </button>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-4xl font-bold text-text mb-8">My Profile</h1>

        <div className="flex flex-col lg:flex-row gap-8">
          {/* Sidebar */}
          <div className="lg:w-64">
            <div className="bg-card rounded-lg p-4">
              <div className="flex items-center mb-6">
                <div className="w-16 h-16 bg-primary rounded-full flex items-center justify-center text-white text-2xl font-bold">
                  JD
                </div>
                <div className="ml-4">
                  <h2 className="text-lg font-semibold text-text">John Doe</h2>
                  <p className="text-text-muted text-sm">Eco Warrior</p>
                </div>
              </div>
              <nav className="space-y-2">
                {tabs.map((tab) => (
                  <button
                    key={tab.id}
                    onClick={() => setActiveTab(tab.id)}
                    className={`w-full flex items-center px-4 py-3 rounded-lg transition ${
                      activeTab === tab.id
                        ? 'bg-primary text-white'
                        : 'text-text hover:bg-card/80'
                    }`}
                  >
                    <span className="mr-3">{tab.icon}</span>
                    {tab.label}
                  </button>
                ))}
              </nav>
            </div>
          </div>

          {/* Main Content */}
          <div className="flex-1">
            {renderContent()}
          </div>
        </div>
      </div>
    </div>
  );
}
