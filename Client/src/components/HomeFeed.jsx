import { useEffect, useState } from 'react';

const dummyTrips = [
  {
    id: 1,
    user: 'Anjaan',
    destination: 'Lakshadweep Islands',
    image: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=7d9f2b0d9b1e7a3c4d8f3b3f0b6f6f7a',
  },
  {
    id: 2,
    user: 'Anjaan',
    destination: 'Rann of Kutch',
    image: 'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=2b8b4b7f2b8b4c6b7b6c7d8f9e0a1b2c',
  },
  {
    id: 3,
    user: 'Anjaan',
    destination: 'Valley of Flowers',
    image: 'https://images.unsplash.com/photo-1501785888041-af3ef285b470?q=80&w=1600&auto=format&fit=crop&ixlib=rb-4.0.3&s=1a2b3c4d5e6f7g8h9i0j',
  },
];

export default function HomeFeed() {
  const [trips, setTrips] = useState([]);

  useEffect(() => {
    // Simulate fetching dummy data
    const timer = setTimeout(() => setTrips(dummyTrips), 300);
    return () => clearTimeout(timer);
  }, []);

  return (
    <section className="mt-12">
      <h2 className="text-2xl font-semibold mb-6">Trips from Travelers</h2>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
        {trips.map((t) => (
          <article
            key={t.id}
            className="overflow-hidden shadow-lg"
            style={{ borderRadius: '24px 24px 24px 4px', backgroundColor: 'var(--tw-color-card)' }}
          >
            <div className="relative h-48 w-full">
              <img
                src={t.image}
                alt={t.destination}
                className="object-cover w-full h-full"
                style={{ borderTopLeftRadius: '24px', borderTopRightRadius: '24px' }}
              />
            </div>

            <div className="p-4 text-text">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-text/80">{t.user}</p>
                  <h3 className="text-lg font-semibold mt-1">{t.destination}</h3>
                </div>

                <button
                  className="ml-4 px-4 py-2 rounded-md font-semibold text-white"
                  style={{ backgroundColor: 'var(--tw-color-primary)' }}
                >
                  Join
                </button>
              </div>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}
