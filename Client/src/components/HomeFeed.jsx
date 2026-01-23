import { useEffect, useState } from 'react';

const dummyProducts = [
  {
    id: 1,
    name: 'Bamboo Water Bottle',
    seller: 'EcoEssentials',
    price: 24.99,
    image: 'https://images.unsplash.com/photo-1602143407151-7111542de0e8?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=7d9f2b0d9b1e7a3c4d8f3b3f0b6f6f7a',
    ecoScore: 95,
  },
  {
    id: 2,
    name: 'Reusable Produce Bags',
    seller: 'GreenLiving',
    price: 12.99,
    image: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=2b8b4b7f2b8b4c6b7b6c7d8f9e0a1b2c',
    ecoScore: 88,
  },
  {
    id: 3,
    name: 'Solar Power Bank',
    seller: 'SunEnergy',
    price: 45.99,
    image: 'https://images.unsplash.com/photo-1591025811588-6a9e5b3b5b3b?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=1a2b3c4d5e6f7g8h9i0j',
    ecoScore: 92,
  },
];

export default function HomeFeed() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    // Simulate fetching dummy data
    const timer = setTimeout(() => setProducts(dummyProducts), 300);
    return () => clearTimeout(timer);
  }, []);

  return (
    <section className="mt-12">
      <h2 className="text-2xl font-semibold text-text mb-6">Featured Eco Products</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {products.map((product) => (
          <article
            key={product.id}
            className="overflow-hidden shadow-lg bg-card"
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
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-text-muted">{product.seller}</p>
                  <h3 className="text-lg font-semibold mt-1">{product.name}</h3>
                  <p className="text-primary font-bold mt-2">${product.price}</p>
                </div>

                <button
                  className="ml-4 px-4 py-2 rounded-md font-semibold text-white bg-primary hover:opacity-90 transition"
                >
                  Buy Now
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
