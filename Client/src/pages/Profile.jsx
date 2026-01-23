import { motion } from 'framer-motion';
import CacunContainer from '../components/CacunContainer';
import { Star, MapPin, Calendar, Award, Users, Shield } from 'lucide-react';

export default function Profile() {
  const reviews = [
    {
      id: 1,
      reviewer: 'Mike Johnson',
      trip: 'Bali Adventure 2024',
      date: 'Jan 2024',
      behavior: 5,
      problemSolving: 4,
      vibeCheck: 5,
      comment: 'Amazing travel partner! Very organized and great at finding local experiences.',
    },
    {
      id: 2,
      reviewer: 'Sarah Chen',
      trip: 'Tokyo Food Tour',
      date: 'Dec 2023',
      behavior: 5,
      problemSolving: 5,
      vibeCheck: 4,
      comment: 'Great sense of direction and always positive! Made the trip memorable.',
    },
  ];

  const stats = {
    tripsCompleted: 12,
    totalCountries: 8,
    averageRating: 4.8,
    reliabilityScore: 95,
  };

  return (
    <div className="space-y-6">
      {/* Profile Header */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <CacunContainer>
          <div className="flex items-center space-x-4">
            <div className="w-20 h-20 rounded-full bg-primary/20 flex items-center justify-center">
              <span className="text-primary text-2xl font-bold">AK</span>
            </div>
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-text">Alex Kumar</h2>
              <p className="text-text-muted">@alexkumar • Travel Enthusiast</p>
              <div className="flex items-center space-x-4 mt-2 text-sm text-text-muted">
                <span className="flex items-center space-x-1">
                  <MapPin className="w-4 h-4" />
                  <span>San Francisco, CA</span>
                </span>
                <span className="flex items-center space-x-1">
                  <Calendar className="w-4 h-4" />
                  <span>Joined Jan 2023</span>
                </span>
              </div>
            </div>
            <button className="px-6 py-2 bg-primary hover:opacity-90 text-white rounded-lg font-semibold transition">
              Edit Profile
            </button>
          </div>
        </CacunContainer>
      </motion.div>

      {/* Stats Grid */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <CacunContainer>
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">{stats.tripsCompleted}</div>
              <p className="text-text-muted text-sm mt-1">Trips Completed</p>
            </div>
          </CacunContainer>
          <CacunContainer>
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">{stats.totalCountries}</div>
              <p className="text-text-muted text-sm mt-1">Countries Visited</p>
            </div>
          </CacunContainer>
          <CacunContainer>
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">{stats.averageRating}</div>
              <p className="text-text-muted text-sm mt-1">Average Rating</p>
            </div>
          </CacunContainer>
          <CacunContainer>
            <div className="text-center">
              <div className="text-2xl font-bold text-primary">{stats.reliabilityScore}%</div>
              <p className="text-text-muted text-sm mt-1">Reliability</p>
            </div>
          </CacunContainer>
        </div>
      </motion.div>

      {/* About Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <CacunContainer>
          <h3 className="text-lg font-bold text-text mb-3">About Me</h3>
          <p className="text-text-muted">
            Adventure seeker and culture enthusiast! Love exploring off-the-beaten-path destinations 
            and meeting fellow travelers. I'm organized, respectful of local customs, and always up 
            for trying new foods. Looking for travel buddies who share similar values!
          </p>
          <div className="flex flex-wrap gap-2 mt-4">
            {['Adventure', 'Photography', 'Food Tours', 'Hiking', 'Cultural Sites'].map((interest) => (
              <span key={interest} className="px-3 py-1 bg-primary/20 text-primary rounded-full text-sm">
                {interest}
              </span>
            ))}
          </div>
        </CacunContainer>
      </motion.div>

      {/* Travel Reviews */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <CacunContainer>
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-bold text-text">Travel Partner Reviews</h3>
            <div className="flex items-center space-x-1">
              <Star className="w-5 h-5 fill-primary text-primary" />
              <span className="font-semibold text-primary">{stats.averageRating}</span>
              <span className="text-text-muted text-sm">(24 reviews)</span>
            </div>
          </div>
          
          <div className="space-y-4">
            {reviews.map((review) => (
              <div key={review.id} className="border-l-4 border-primary/30 pl-4">
                <div className="flex items-center justify-between mb-2">
                  <div>
                    <h4 className="font-semibold text-text">{review.reviewer}</h4>
                    <p className="text-text-muted text-sm">{review.trip} • {review.date}</p>
                  </div>
                  <div className="flex items-center space-x-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star 
                        key={star} 
                        className={`w-4 h-4 ${
                          star <= Math.round((review.behavior + review.problemSolving + review.vibeCheck) / 3)
                            ? 'fill-primary text-primary' 
                            : 'text-primary/30'
                        }`} 
                      />
                    ))}
                  </div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 mb-3 text-sm">
                  <div className="flex items-center space-x-2">
                    <Users className="w-4 h-4 text-primary/60" />
                    <span className="text-text-muted">Behavior:</span>
                    <div className="flex space-x-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star 
                          key={star} 
                          className={`w-3 h-3 ${
                            star <= review.behavior ? 'fill-primary text-primary' : 'text-primary/30'
                          }`} 
                        />
                      ))}
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Shield className="w-4 h-4 text-primary/60" />
                    <span className="text-text-muted">Problem Solving:</span>
                    <div className="flex space-x-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star 
                          key={star} 
                          className={`w-3 h-3 ${
                            star <= review.problemSolving ? 'fill-primary text-primary' : 'text-primary/30'
                          }`} 
                        />
                      ))}
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <Award className="w-4 h-4 text-primary/60" />
                    <span className="text-text-muted">Vibe Check:</span>
                    <div className="flex space-x-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <Star 
                          key={star} 
                          className={`w-3 h-3 ${
                            star <= review.vibeCheck ? 'fill-primary text-primary' : 'text-primary/30'
                          }`} 
                        />
                      ))}
                    </div>
                  </div>
                </div>
                
                <p className="text-text-muted text-sm">{review.comment}</p>
              </div>
            ))}
          </div>
        </CacunContainer>
      </motion.div>
    </div>
  );
}
