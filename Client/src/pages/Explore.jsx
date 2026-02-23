import { useState, useMemo } from 'react';
import { Search, MapPin, Calendar, Users, Filter, ChevronLeft, ChevronRight, Heart, Star, Send, User } from 'lucide-react';
import CacunContainer from '../components/CacunContainer';

export default function Explore() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [priceRange, setPriceRange] = useState([0, 5000]);
  const [groupSize, setGroupSize] = useState('all');
  const [sortBy, setSortBy] = useState('newest');
  const [currentPage, setCurrentPage] = useState(1);
  const [savedTrips, setSavedTrips] = useState([]);
  const [showFilters, setShowFilters] = useState(false);
  const [requests, setRequests] = useState([]);
  
  const tripsPerPage = 6;
  
  const categories = [
    'All', 'Mountains', 'Beaches', 'Trekking', 'Road Trip', 
    'Cultural', 'Adventure', 'Relaxation', 'City Tour', 'Wildlife'
  ];

  const trips = [
    {
      id: 1,
      destination: 'Bali, Indonesia',
      date: 'Dec 15-22, 2024',
      budget: 1200,
      groupSize: 4,
      maxGroupSize: 6,
      organizer: 'Mike Johnson',
      organizerAvatar: 'mike',
      rating: 4.8,
      description: 'Exploring temples, beaches, and rice terraces. Looking for adventure buddies!',
      category: 'Beaches',
      image: 'bali-temple',
      tags: ['Adventure', 'Culture', 'Beach'],
      createdAt: new Date('2024-11-01'),
      difficulty: 'Moderate',
      accommodation: 'Hotel',
      transport: 'Flight',
    },
    {
      id: 2,
      destination: 'Tokyo, Japan',
      date: 'Jan 8-15, 2025',
      budget: 2000,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Yuki Tanaka',
      organizerAvatar: 'yuki',
      rating: 4.9,
      description: 'Cherry blossom season! Food tours, temples, and modern culture.',
      category: 'Cultural',
      image: 'tokyo-cherry',
      tags: ['Culture', 'Food', 'Photography'],
      createdAt: new Date('2024-10-15'),
      difficulty: 'Easy',
      accommodation: 'Ryokan',
      transport: 'Train',
    },
    {
      id: 3,
      destination: 'Paris, France',
      date: 'Feb 14-21, 2025',
      budget: 1800,
      groupSize: 3,
      maxGroupSize: 5,
      organizer: 'Marie Dubois',
      organizerAvatar: 'marie',
      rating: 4.7,
      description: 'Romantic getaway with museums, cafes, and day trips to Versailles.',
      category: 'Cultural',
      image: 'paris-eiffel',
      tags: ['Romance', 'Art', 'History'],
      createdAt: new Date('2024-10-20'),
      difficulty: 'Easy',
      accommodation: 'Boutique Hotel',
      transport: 'Metro',
    },
    {
      id: 4,
      destination: 'Swiss Alps',
      date: 'Mar 10-17, 2025',
      budget: 2500,
      groupSize: 5,
      maxGroupSize: 8,
      organizer: 'Hans Mueller',
      organizerAvatar: 'hans',
      rating: 4.9,
      description: 'Skiing, snowboarding, and mountain hiking in the heart of Alps.',
      category: 'Mountains',
      image: 'swiss-alps',
      tags: ['Skiing', 'Adventure', 'Nature'],
      createdAt: new Date('2024-09-25'),
      difficulty: 'Hard',
      accommodation: 'Mountain Lodge',
      transport: 'Train',
    },
    {
      id: 5,
      destination: 'Santorini, Greece',
      date: 'Apr 5-12, 2025',
      budget: 1600,
      groupSize: 3,
      maxGroupSize: 6,
      organizer: 'Elena Papadopoulos',
      organizerAvatar: 'elena',
      rating: 4.8,
      description: 'White buildings, blue domes, and stunning sunsets over the Aegean.',
      category: 'Beaches',
      image: 'santorini-sunset',
      tags: ['Beach', 'Romance', 'Photography'],
      createdAt: new Date('2024-09-30'),
      difficulty: 'Easy',
      accommodation: 'Villa',
      transport: 'Ferry',
    },
    {
      id: 6,
      destination: 'Machu Picchu, Peru',
      date: 'May 20-27, 2025',
      budget: 2200,
      groupSize: 4,
      maxGroupSize: 8,
      organizer: 'Carlos Rodriguez',
      organizerAvatar: 'carlos',
      rating: 4.9,
      description: 'Ancient Inca trail, sacred valley, and sunrise at Machu Picchu.',
      category: 'Trekking',
      image: 'machu-picchu',
      tags: ['Trekking', 'History', 'Adventure'],
      createdAt: new Date('2024-09-10'),
      difficulty: 'Hard',
      accommodation: 'Camping',
      transport: 'Bus',
    },
    {
      id: 7,
      destination: 'Dubai, UAE',
      date: 'Jun 1-8, 2025',
      budget: 2800,
      groupSize: 2,
      maxGroupSize: 4,
      organizer: 'Ahmed Al-Mansoori',
      organizerAvatar: 'ahmed',
      rating: 4.6,
      description: 'Modern architecture, luxury shopping, and desert safari adventures.',
      category: 'City Tour',
      image: 'dubai-skyline',
      tags: ['Luxury', 'Shopping', 'Adventure'],
      createdAt: new Date('2024-08-25'),
      difficulty: 'Easy',
      accommodation: '5-Star Hotel',
      transport: 'Taxi',
    },
    {
      id: 8,
      destination: 'Iceland',
      date: 'Jul 15-22, 2025',
      budget: 3000,
      groupSize: 6,
      maxGroupSize: 10,
      organizer: 'Bjorn Eriksson',
      organizerAvatar: 'bjorn',
      rating: 5.0,
      description: 'Northern lights, glaciers, hot springs, and dramatic landscapes.',
      category: 'Adventure',
      image: 'iceland-aurora',
      tags: ['Nature', 'Photography', 'Adventure'],
      createdAt: new Date('2024-08-15'),
      difficulty: 'Moderate',
      accommodation: 'Guesthouse',
      transport: 'Rental Car',
    },
  ];

  const filteredAndSortedTrips = useMemo(() => {
    let filtered = trips.filter(trip => {
      const matchesCategory = selectedCategory === 'All' || trip.category === selectedCategory;
      const matchesSearch = searchQuery === '' || 
        trip.destination.toLowerCase().includes(searchQuery.toLowerCase()) ||
        trip.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        trip.organizer.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesPrice = trip.budget >= priceRange[0] && trip.budget <= priceRange[1];
      const matchesGroupSize = groupSize === 'all' || 
        (groupSize === 'small' && trip.maxGroupSize <= 4) ||
        (groupSize === 'medium' && trip.maxGroupSize >= 5 && trip.maxGroupSize <= 8) ||
        (groupSize === 'large' && trip.maxGroupSize > 8);
      
      return matchesCategory && matchesSearch && matchesPrice && matchesGroupSize;
    });

    // Sort trips
    filtered.sort((a, b) => {
      switch(sortBy) {
        case 'newest':
          return b.createdAt - a.createdAt;
        case 'oldest':
          return a.createdAt - b.createdAt;
        case 'price-low':
          return a.budget - b.budget;
        case 'price-high':
          return b.budget - a.budget;
        case 'rating':
          return b.rating - a.rating;
        default:
          return 0;
      }
    });

    return filtered;
  }, [trips, selectedCategory, searchQuery, priceRange, groupSize, sortBy]);

  // Pagination
  const totalPages = Math.ceil(filteredAndSortedTrips.length / tripsPerPage);
  const startIndex = (currentPage - 1) * tripsPerPage;
  const paginatedTrips = filteredAndSortedTrips.slice(startIndex, startIndex + tripsPerPage);

  const handleSaveTrip = (tripId) => {
    setSavedTrips(prev => 
      prev.includes(tripId) 
        ? prev.filter(id => id !== tripId)
        : [...prev, tripId]
    );
  };

  const handleRequestToJoin = (tripId) => {
    if (requests.includes(tripId)) {
      alert('You have already requested to join this trip!');
      return;
    }
    setRequests(prev => [...prev, tripId]);
    alert('Request sent successfully! The organizer will contact you soon.');
  };

  return (
    <div className="explore-container max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      {/* Search Section */}
      <div className="slide-in-up">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-3xl font-bold text-text">Explore Trips</h1>
          <div className="flex items-center gap-4">
            <span className="text-text-muted text-sm">
              {filteredAndSortedTrips.length} trips found
            </span>
            <button
              onClick={() => setShowFilters(!showFilters)}
              className="flex items-center gap-2 px-4 py-2 bg-card border border-primary/20 rounded-xl text-text hover:bg-primary/10 transition"
            >
              <Filter className="w-4 h-4" />
              Filters
            </button>
          </div>
        </div>
        
        {/* Search Bar */}
        <div className="search-bar mb-6">
          <Search className="search-icon w-5 h-5" />
          <input
            type="text"
            placeholder="Search destinations, descriptions, or organizers..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setCurrentPage(1);
            }}
            className="search-input"
          />
        </div>

        {/* Category Pills */}
        <div className="category-pills mb-6">
          {categories.map((category) => (
            <button
              key={category}
              onClick={() => {
                setSelectedCategory(category);
                setCurrentPage(1);
              }}
              className={`category-pill ${selectedCategory === category ? 'active' : ''}`}
            >
              {category}
            </button>
          ))}
        </div>

        {/* Advanced Filters */}
        {showFilters && (
          <CacunContainer className="glass-container mb-6">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
              <div>
                <label className="block text-text text-sm font-medium mb-2">Price Range</label>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    placeholder="Min"
                    value={priceRange[0]}
                    onChange={(e) => setPriceRange([parseInt(e.target.value) || 0, priceRange[1]])}
                    className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                  />
                  <span className="text-text-muted">-</span>
                  <input
                    type="number"
                    placeholder="Max"
                    value={priceRange[1]}
                    onChange={(e) => setPriceRange([priceRange[0], parseInt(e.target.value) || 5000])}
                    className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-text text-sm font-medium mb-2">Group Size</label>
                <select
                  value={groupSize}
                  onChange={(e) => {
                    setGroupSize(e.target.value);
                    setCurrentPage(1);
                  }}
                  className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                >
                  <option value="all">All Sizes</option>
                  <option value="small">Small (4 or less)</option>
                  <option value="medium">Medium (5-8)</option>
                  <option value="large">Large (8+)</option>
                </select>
              </div>
              
              <div>
                <label className="block text-text text-sm font-medium mb-2">Sort By</label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="w-full px-3 py-2 bg-background border border-white/20 rounded-lg text-text"
                >
                  <option value="newest">Newest First</option>
                  <option value="oldest">Oldest First</option>
                  <option value="price-low">Price: Low to High</option>
                  <option value="price-high">Price: High to Low</option>
                  <option value="rating">Highest Rated</option>
                </select>
              </div>
              
              <div className="flex items-end">
                <button
                  onClick={() => {
                    setPriceRange([0, 5000]);
                    setGroupSize('all');
                    setSortBy('newest');
                    setSearchQuery('');
                    setSelectedCategory('All');
                    setCurrentPage(1);
                  }}
                  className="w-full px-4 py-2 bg-primary/20 text-primary rounded-lg hover:bg-primary/30 transition"
                >
                  Reset Filters
                </button>
              </div>
            </div>
          </CacunContainer>
        )}
      </div>

      {/* Results */}
      <div className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {paginatedTrips.map((trip, index) => (
            <div 
              key={trip.id} 
              className={`trip-card slide-in-up-delay-${Math.min(index + 1, 3)}`}
            >
              {/* Cover Image */}
              <div className="relative">
                <img 
                  src={`https://picsum.photos/seed/${trip.image}/600/300.jpg`}
                  alt={trip.destination}
                  className="trip-card-image"
                />
                <div className="absolute top-4 right-4 flex items-center gap-2">
                  <button
                    onClick={() => handleSaveTrip(trip.id)}
                    className={`p-2 rounded-lg backdrop-blur-sm transition ${
                      savedTrips.includes(trip.id)
                        ? 'bg-primary/20 text-primary'
                        : 'bg-black/20 text-white hover:bg-black/30'
                    }`}
                  >
                    <Heart className={`w-4 h-4 ${savedTrips.includes(trip.id) ? 'fill-current' : ''}`} />
                  </button>
                </div>
                <div className="absolute bottom-4 left-4">
                  <span className="px-3 py-1 bg-primary/90 text-white text-sm rounded-full backdrop-blur-sm">
                    {trip.category}
                  </span>
                </div>
              </div>

              {/* Trip Content */}
              <div className="space-y-4">
                {/* Header */}
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-xl font-bold text-text mb-1">
                      {trip.destination}
                    </h2>
                    <div className="flex items-center gap-2">
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 fill-primary text-primary" />
                        <span className="text-sm text-text">{trip.rating}</span>
                      </div>
                      <span className="text-text-muted text-sm">•</span>
                      <span className="text-text-muted text-sm">by {trip.organizer}</span>
                    </div>
                  </div>
                  <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                    <span className="text-primary font-semibold text-sm">
                      {trip.organizer.slice(0, 1).toUpperCase()}
                    </span>
                  </div>
                </div>

                {/* Description */}
                <p className="text-text-muted text-sm line-clamp-2">
                  {trip.description}
                </p>

                {/* Tags */}
                <div className="flex flex-wrap gap-2">
                  {trip.tags.map((tag, tagIndex) => (
                    <span
                      key={tagIndex}
                      className="px-2 py-1 bg-background/50 border border-white/20 rounded-full text-xs text-text"
                    >
                      {tag}
                    </span>
                  ))}
                </div>

                {/* Trip Details */}
                <div className="trip-details">
                  <div className="grid grid-cols-2 gap-3 text-sm">
                    <div className="flex items-center space-x-2 text-text-muted">
                      <Calendar className="w-4 h-4 text-primary" />
                      <span>{trip.date}</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-muted">
                      <Users className="w-4 h-4 text-primary" />
                      <span>{trip.groupSize}/{trip.maxGroupSize} people</span>
                    </div>
                    <div className="flex items-center space-x-2 text-text-muted">
                      <span className="text-text-muted">Difficulty:</span>
                      <span className="text-text font-medium">{trip.difficulty}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="text-text-muted">Budget:</span>
                      <span className="trip-budget">${trip.budget}</span>
                    </div>
                  </div>
                </div>

                {/* Action Buttons */}
                <div className="flex gap-3">
                  <button 
                    onClick={() => handleRequestToJoin(trip.id)}
                    className={`flex-1 py-3 rounded-xl font-semibold transition ${
                      requests.includes(trip.id)
                        ? 'bg-green-500/20 text-green-500 border border-green-500/30'
                        : 'request-button'
                    }`}
                  >
                    {requests.includes(trip.id) ? 'Request Sent ✓' : 'Request to Join'}
                  </button>
                  <button className="p-3 rounded-xl bg-background/50 border border-white/20 text-text hover:bg-background/70 transition">
                    <Send className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {paginatedTrips.length === 0 && (
          <div className="text-center py-12">
            <div className="text-text-muted text-lg">
              No trips found matching your criteria.
            </div>
            <p className="text-text-muted mt-2">
              Try adjusting your search or filters.
            </p>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-8">
            <button
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              className="p-2 rounded-lg bg-background/50 border border-white/20 text-text hover:bg-background/70 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            
            {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
              <button
                key={page}
                onClick={() => setCurrentPage(page)}
                className={`w-8 h-8 rounded-lg text-sm font-medium transition ${
                  currentPage === page
                    ? 'bg-primary text-white'
                    : 'bg-background/50 border border-white/20 text-text hover:bg-background/70'
                }`}
              >
                {page}
              </button>
            ))}
            
            <button
              onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              disabled={currentPage === totalPages}
              className="p-2 rounded-lg bg-background/50 border border-white/20 text-text hover:bg-background/70 transition disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
