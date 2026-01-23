import React from "react";

const dummyTrips = [
  {
    id: 1,
    user: "Anjaan",
    destination: "Goa Beach",
    image: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80",
  },
  {
    id: 2,
    user: "Anjaan",
    destination: "Manali Hills",
    image: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=crop&w=400&q=80",
  },
];

export default function HomeFeed() {
  return (
    <div className="w-full max-w-md flex flex-col gap-6">
      {dummyTrips.map((trip) => (
        <div
          key={trip.id}
          className="bg-card rounded-[24px_24px_24px_4px] p-6 flex flex-col items-center shadow-lg"
          style={{ borderRadius: "24px 24px 24px 4px" }}
        >
          <img src={trip.image} alt={trip.destination} className="w-full h-40 object-cover rounded-2xl mb-4" />
          <div className="text-text-main text-lg font-bold mb-1">{trip.user}</div>
          <div className="text-text-muted text-md mb-3">{trip.destination}</div>
          <button className="bg-primary-special text-text-main px-6 py-2 rounded-full font-semibold shadow-md hover:scale-105 transition-transform">Join</button>
        </div>
      ))}
    </div>
  );
}
