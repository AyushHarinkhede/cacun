import { useState } from 'react';
import { Search, MapPin, Calendar, Users } from 'lucide-react';

export default function Explore() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  
  const categories = [
    'All', 'Mountains', 'Beaches', 'Trekking', 'Road Trip', 
    'Cultural', 'Adventure', 'Relaxation', 'City Tour', 'Wildlife'
  ];

  const trips = [
    {
      id: 1,
      destination: 'Bali, Indonesia',
      date: 'Dec 15-22, 2024',
      budget: '$1,200',
      groupSize: '4 people',
      organizer: 'Mike Johnson',
      description: 'Exploring temples, beaches, and rice terraces. Looking for adventure buddies!',
      category: 'Beaches'
    },
    {
      id: 2,
      destination: 'Tokyo, Japan',
      date: 'Jan 8-15, 2025',
      budget: '$2,000',
      groupSize: '2 people',
      organizer: 'Yuki Tanaka',
      description: 'Cherry blossom season! Food tours, temples, and modern culture.',
      category: 'Cultural'
    },
    {
      id: 3,
      destination: 'Paris, France',
      date: 'Feb 14-21, 2025',
      budget: '$1,800',
      groupSize: '3 people',
      organizer: 'Marie Dubois',
      description: 'Romantic getaway with museums, cafes, and day trips to Versailles.',
      category: 'Cultural'
    },
  ];

  const filteredTrips = trips.filter(trip => 
    (selectedCategory === 'All' || trip.category === selectedCategory) &&
    (searchQuery === '' || trip.destination.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="explore-container">
      {/* Search Section */}
      <div className="slide-in-up">
        <h1 className="text-3xl font-bold text-text mb-6">Explore Trips</h1>
        
        {/* Search Bar */}
        <div className="search-bar">
          <Search className="search-icon w-5 h-5" />
          <input
            type="text"
            placeholder="Search destinations..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="search-input"
          />
        </div>

        {/* Category Pills */}
        <div className="category-pills">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => setSelectedCategory(category)}
              className={`category-pill ${selectedCategory === category ? 'active' : ''}`}
            >
              {category}
            </button>
          ))}
        </div>
      </div>

      {/* Results */}
      <div className="space-y-6">
        {filteredTrips.map((trip, index) => (
          <div 
            key={trip.id} 
            className={`trip-card slide-in-up-delay-${index + 1}`}
          >
            {/* Cover Image */}
            <img 
              src={'/default.jpg'} 
              alt={trip.destination}
              className="trip-card-image"
              onError={(e) => {
                e.target.src = `https://picsum.photos/seed/${trip.destination}/400/200.jpg`;
              }}
            />

            {/* Trip Content */}
            <div className="space-y-4">
              {/* Category Badge */}
              <div className="flex items-center justify-between">
                <span className="px-3 py-1 bg-primary/20 text-primary text-sm rounded-full">
                  {trip.category}
                </span>
                <span className="text-text-muted text-sm">by {trip.organizer}</span>
              </div>

              {/* Trip Details */}
              <div className="trip-details">
                <h2 className="text-2xl font-bold text-text mb-2">
                  {trip.destination}
                </h2>
                
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                  <div className="flex items-center space-x-2 text-text-muted">
                    <Calendar className="w-4 h-4 text-primary" />
                    <span>{trip.date}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-text-muted">
                    <Users className="w-4 h-4 text-primary" />
                    <span>{trip.groupSize}</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className="text-text-muted">Budget:</span>
                    <span className="trip-budget">{trip.budget}</span>
                  </div>
                </div>
              </div>

              {/* Action Button */}
              <button className="request-button">
                Request to Join
              </button>
            </div>
          </div>
        ))}

        {filteredTrips.length === 0 && (
          <div className="text-center py-12">
            <div className="text-text-muted text-lg">
              No trips found matching your criteria.
            </div>
            <p className="text-text-muted mt-2">
              Try adjusting your search or filters.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
