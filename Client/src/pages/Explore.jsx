import { motion } from 'framer-motion';
import CacunContainer from '../components/CacunContainer';
import { MapPin, Calendar, Users, DollarSign, Search, Filter } from 'lucide-react';

export default function Explore() {
  const trips = [
    {
      id: 1,
      destination: 'Bali, Indonesia',
      date: 'Dec 15-22, 2024',
      budget: '$1,200',
      groupSize: '4 people',
      organizer: 'Mike Johnson',
      description: 'Exploring temples, beaches, and rice terraces. Looking for adventure buddies!',
    },
    {
      id: 2,
      destination: 'Tokyo, Japan',
      date: 'Jan 8-15, 2025',
      budget: '$2,000',
      groupSize: '2 people',
      organizer: 'Yuki Tanaka',
      description: 'Cherry blossom season! Food tours, temples, and modern culture.',
    },
    {
      id: 3,
      destination: 'Paris, France',
      date: 'Feb 14-21, 2025',
      budget: '$1,800',
      groupSize: '3 people',
      organizer: 'Marie Dubois',
      description: 'Romantic getaway with museums, cafes, and day trips to Versailles.',
    },
  ];

  return (
    <div className="space-y-6">
      {/* Search and Filter */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <CacunContainer>
          <h2 className="text-xl font-bold text-text mb-4">Explore Trips</h2>
          <div className="space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-text-muted" />
              <input
                type="text"
                placeholder="Search destinations..."
                className="w-full pl-10 pr-4 py-3 bg-background border border-primary/20 rounded-lg text-text placeholder-text-muted"
              />
            </div>
            <div className="flex gap-3">
              <button className="flex items-center space-x-2 px-4 py-2 bg-background border border-primary/20 rounded-lg text-text hover:bg-primary/10 transition">
                <Filter className="w-4 h-4" />
                <span>Filters</span>
              </button>
              <select className="px-4 py-2 bg-background border border-primary/20 rounded-lg text-text">
                <option>All Dates</option>
                <option>This Month</option>
                <option>Next Month</option>
                <option>Next 3 Months</option>
              </select>
              <select className="px-4 py-2 bg-background border border-primary/20 rounded-lg text-text">
                <option>Any Budget</option>
                <option>Under $1000</option>
                <option>$1000-2000</option>
                <option>Over $2000</option>
              </select>
            </div>
          </div>
        </CacunContainer>
      </motion.div>

      {/* Trip Cards */}
      {trips.map((trip, index) => (
        <motion.div
          key={trip.id}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 * (index + 1) }}
        >
          <CacunContainer hover>
            <div className="space-y-4">
              {/* Header */}
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-lg font-bold text-text">{trip.destination}</h3>
                  <p className="text-text-muted text-sm">Organized by {trip.organizer}</p>
                </div>
                <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
                  <span className="text-primary text-xs font-semibold">
                    {trip.organizer.split(' ').map(n => n[0]).join('')}
                  </span>
                </div>
              </div>

              {/* Description */}
              <p className="text-text">{trip.description}</p>

              {/* Trip Details */}
              <div className="grid grid-cols-3 gap-4 text-sm">
                <div className="flex items-center space-x-2 text-text-muted">
                  <Calendar className="w-4 h-4" />
                  <span>{trip.date}</span>
                </div>
                <div className="flex items-center space-x-2 text-text-muted">
                  <DollarSign className="w-4 h-4" />
                  <span>{trip.budget}</span>
                </div>
                <div className="flex items-center space-x-2 text-text-muted">
                  <Users className="w-4 h-4" />
                  <span>{trip.groupSize}</span>
                </div>
              </div>

              {/* Action Button */}
              <button className="w-full bg-primary hover:opacity-90 text-white py-3 rounded-lg font-semibold transition">
                Request to Join
              </button>
            </div>
          </CacunContainer>
        </motion.div>
      ))}
    </div>
  );
}
