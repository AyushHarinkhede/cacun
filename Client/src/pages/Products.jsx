import { useState } from 'react';

const categories = ['All', 'Home & Living', 'Personal Care', 'Fashion', 'Electronics', 'Food & Beverages'];

const dummyProducts = [
  {
    id: 1,
    name: 'Bamboo Water Bottle',
    seller: 'EcoEssentials',
    price: 24.99,
    category: 'Home & Living',
    image: 'https://images.unsplash.com/photo-1602143407151-7111542de0e8?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=7d9f2b0d9b1e7a3c4d8f3b3f0b6f6f7a',
    ecoScore: 95,
    description: 'Sustainable bamboo water bottle with stainless steel interior',
  },
  {
    id: 2,
    name: 'Reusable Produce Bags',
    seller: 'GreenLiving',
    price: 12.99,
    category: 'Home & Living',
    image: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=2b8b4b7f2b8b4c6b7b6c7d8f9e0a1b2c',
    ecoScore: 88,
    description: 'Set of 5 reusable mesh produce bags for shopping',
  },
  {
    id: 3,
    name: 'Solar Power Bank',
    seller: 'SunEnergy',
    price: 45.99,
    category: 'Electronics',
    image: 'https://images.unsplash.com/photo-1591025811588-6a9e5b3b5b3b?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=1a2b3c4d5e6f7g8h9i0j',
    ecoScore: 92,
    description: '20000mAh solar-powered portable charger',
  },
  {
    id: 4,
    name: 'Organic Cotton T-Shirt',
    seller: 'EcoWear',
    price: 29.99,
    category: 'Fashion',
    image: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=3c4d5e6f7g8h9i0j1k2l',
    ecoScore: 90,
    description: '100% organic cotton comfortable t-shirt',
  },
  {
    id: 5,
    name: 'Natural Shampoo Bar',
    seller: 'PureBeauty',
    price: 8.99,
    category: 'Personal Care',
    image: 'https://images.unsplash.com/photo-1522337360788-8b13dee7a37e?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=4d5e6f7g8h9i0j1k2l3m',
    ecoScore: 94,
    description: 'Plastic-free natural shampoo bar with essential oils',
  },
  {
    id: 6,
    name: 'Biodegradable Phone Case',
    seller: 'TechEco',
    price: 19.99,
    category: 'Electronics',
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=5e6f7g8h9i0j1k2l3m4n',
    ecoScore: 87,
    description: 'Compostable phone case made from plant materials',
  },
];

export default function Products() {
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [searchTerm, setSearchTerm] = useState('');

  const filteredProducts = dummyProducts.filter(product => {
    const matchesCategory = selectedCategory === 'All' || product.category === selectedCategory;
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         product.seller.toLowerCase().includes(searchTerm.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div className="min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-4xl font-bold text-text mb-8">Eco Products</h1>

        {/* Search and Filter */}
        <div className="mb-8 space-y-4">
          <div className="relative">
            <input
              type="text"
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full px-4 py-3 pl-12 bg-card text-text rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
            />
            <svg className="absolute left-4 top-3.5 w-5 h-5 text-text-muted" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <circle cx="11" cy="11" r="6" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
              <path d="M21 21l-4.35-4.35" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>

          <div className="flex flex-wrap gap-2">
            {categories.map(category => (
              <button
                key={category}
                onClick={() => setSelectedCategory(category)}
                className={`px-4 py-2 rounded-full font-medium transition ${
                  selectedCategory === category
                    ? 'bg-primary text-white'
                    : 'bg-card text-text hover:bg-card/80'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>

        {/* Products Grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredProducts.map((product) => (
            <article
              key={product.id}
              className="overflow-hidden shadow-lg bg-card hover:shadow-xl transition-shadow"
              style={{ borderRadius: '24px 24px 24px 4px' }}
            >
              <div className="relative h-48 w-full">
                <img
                  src={product.image}
                  alt={product.name}
                  className="object-cover w-full h-full"
                  style={{ borderTopLeftRadius: '24px', borderTopRightRadius: '24px' }}
                />
                <div className="absolute top-2 right-2 bg-green-500 text-white px-2 py-1 rounded-full text-xs font-semibold">
                  🌿 {product.ecoScore}%
                </div>
              </div>

              <div className="p-4 text-text">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="text-sm text-text-muted">{product.seller}</p>
                    <h3 className="text-lg font-semibold mt-1">{product.name}</h3>
                    <p className="text-sm text-text-muted mt-2 line-clamp-2">{product.description}</p>
                    <p className="text-primary font-bold mt-2">${product.price}</p>
                  </div>

                  <button
                    className="ml-4 px-4 py-2 rounded-md font-semibold text-white bg-primary hover:opacity-90 transition whitespace-nowrap"
                  >
                    Add to Cart
                  </button>
                </div>
              </div>
            </article>
          ))}
        </div>

        {filteredProducts.length === 0 && (
          <div className="text-center py-12">
            <p className="text-text-muted text-lg">No products found matching your criteria.</p>
          </div>
        )}
      </div>
    </div>
  );
}
