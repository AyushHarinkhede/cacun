import { MapPin, Calendar, Users } from 'lucide-react';

export default function Home() {
  const tripProposals = [
    {
      id: 1,
      destination: 'Bali, Indonesia',
      startDate: 'Dec 15, 2024',
      endDate: 'Dec 22, 2024',
      budget: 1200,
      host: {
        name: 'Sarah Chen',
        avatar: 'SC',
        verified: true
      },
      image: '/bali-temple.jpg',
      description: 'Explore ancient temples and pristine beaches'
    },
    {
      id: 2,
      destination: 'Swiss Alps',
      startDate: 'Jan 8, 2025',
      endDate: 'Jan 15, 2025',
      budget: 2500,
      host: {
        name: 'Mike Johnson',
        avatar: 'MJ',
        verified: true
      },
      image: '/swiss-alps.jpg',
      description: 'Mountain adventure and skiing paradise'
    },
    {
      id: 3,
      destination: 'Tokyo, Japan',
      startDate: 'Feb 14, 2025',
      endDate: 'Feb 21, 2025',
      budget: 1800,
      host: {
        name: 'Yuki Tanaka',
        avatar: 'YT',
        verified: false
      },
      image: '/tokyo-street.jpg',
      description: 'Cherry blossoms and cultural immersion'
    },
    {
      id: 4,
      destination: 'Santorini, Greece',
      startDate: 'Mar 20, 2025',
      endDate: 'Mar 27, 2025',
      budget: 1600,
      host: {
        name: 'Elena Papadopoulos',
        avatar: 'EP',
        verified: true
      },
      image: '/santorini-sunset.jpg',
      description: 'Island hopping and Mediterranean cuisine'
    }
  ];

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="text-center slide-in-up">
        <h1 className="text-4xl font-bold text-text mb-4">
          Find Your Travel Buddy
        </h1>
        <p className="text-text-muted text-lg max-w-2xl mx-auto">
          Connect with verified travelers and explore the world together. 
          Every trip builds your travel reputation.
        </p>
      </div>

      {/* Trip Proposals Feed */}
      <div className="space-y-6">
        {tripProposals.map((trip, index) => (
          <div 
            key={trip.id} 
            className={`trip-card slide-in-up-delay-${index + 1}`}
          >
            {/* Cover Image */}
            <img 
              src={trip.image} 
              alt={trip.destination}
              className="trip-card-image"
              onError={(e) => {
                e.target.src = `https://picsum.photos/seed/${trip.destination}/400/200.jpg`;
              }}
            />

            {/* Trip Content */}
            <div className="space-y-4">
              {/* Host Info */}
              <div className="trip-host-info">
                <div className="trip-host-avatar">
                  <span className="text-primary font-semibold">{trip.host.avatar}</span>
                </div>
                <div className="flex-1">
                  <div className="flex items-center space-x-2">
                    <h3 className="text-text font-semibold">{trip.host.name}</h3>
                    {trip.host.verified && (
                      <span className="verified-badge">
                        ✓ Verified
                      </span>
                    )}
                  </div>
                  <p className="text-text-muted text-sm">Trip Host</p>
                </div>
              </div>

              {/* Trip Details */}
              <div className="trip-details">
                <h2 className="text-2xl font-bold text-text mb-2">
                  {trip.destination}
                </h2>
                <p className="text-text-muted mb-4">
                  {trip.description}
                </p>
                
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                  <div className="flex items-center space-x-2 text-text-muted">
                    <Calendar className="w-4 h-4 text-primary" />
                    <span>{trip.startDate} - {trip.endDate}</span>
                  </div>
                  <div className="flex items-center space-x-2 text-text-muted">
                    <Users className="w-4 h-4 text-primary" />
                    <span>2-4 travelers</span>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className="text-text-muted">Budget:</span>
                    <span className="trip-budget">${trip.budget}/person</span>
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
      </div>
    </div>
  );
}
