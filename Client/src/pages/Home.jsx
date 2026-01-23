import { motion } from 'framer-motion';
import CacunContainer from '../components/CacunContainer';
import { Heart, MessageCircle, MapPin, Calendar, Users, Star } from 'lucide-react';

export default function Home() {
  return (
    <div className="space-y-6">
      {/* Social Feed Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
      >
        <CacunContainer hover>
          <div className="space-y-4">
            {/* User Info */}
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                <span className="text-primary font-semibold">AK</span>
              </div>
              <div>
                <h3 className="text-text font-semibold">Alex Kumar</h3>
                <p className="text-text-muted text-sm">Bali, Indonesia • 2 hours ago</p>
              </div>
            </div>

            {/* Post Content */}
            <div className="space-y-3">
              <p className="text-text">
                Just finished an amazing sunrise trek at Mount Batur! The view was absolutely breathtaking. 
                Anyone planning to visit Bali soon? Would love to share tips! 🌅
              </p>
              
              {/* Image */}
              <div className="w-full h-48 bg-gradient-to-br from-primary/20 to-card rounded-lg"></div>
              
              {/* Engagement Stats */}
              <div className="flex items-center space-x-6 text-text-muted">
                <button className="flex items-center space-x-2 hover:text-primary transition">
                  <Heart className="w-5 h-5" />
                  <span className="text-sm">234</span>
                </button>
                <button className="flex items-center space-x-2 hover:text-primary transition">
                  <MessageCircle className="w-5 h-5" />
                  <span className="text-sm">18</span>
                </button>
                <button className="flex items-center space-x-2 hover:text-primary transition">
                  <MapPin className="w-5 h-5" />
                  <span className="text-sm">Bali</span>
                </button>
              </div>
            </div>
          </div>
        </CacunContainer>
      </motion.div>

      {/* Trip Highlights */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
      >
        <CacunContainer>
          <h2 className="text-xl font-bold text-text mb-4">Trip Highlights</h2>
          <div className="grid grid-cols-3 gap-2">
            {[1, 2, 3].map((i) => (
              <div key={i} className="aspect-square bg-gradient-to-br from-primary/10 to-card rounded-lg"></div>
            ))}
          </div>
        </CacunContainer>
      </motion.div>

      {/* Find Travel Partner */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.3 }}
      >
        <CacunContainer>
          <h2 className="text-xl font-bold text-text mb-4">Find Travel Partner</h2>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <select className="bg-background border border-primary/20 rounded-lg px-3 py-2 text-text">
                <option>Destination</option>
                <option>Bali, Indonesia</option>
                <option>Tokyo, Japan</option>
                <option>Paris, France</option>
              </select>
              <input 
                type="date" 
                className="bg-background border border-primary/20 rounded-lg px-3 py-2 text-text"
              />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <input 
                type="number" 
                placeholder="Budget ($)" 
                className="bg-background border border-primary/20 rounded-lg px-3 py-2 text-text"
              />
              <select className="bg-background border border-primary/20 rounded-lg px-3 py-2 text-text">
                <option>Group Size</option>
                <option>2 people</option>
                <option>3-4 people</option>
                <option>5+ people</option>
              </select>
            </div>
            <button className="w-full bg-primary hover:opacity-90 text-white py-3 rounded-lg font-semibold transition">
              Find Travel Partners
            </button>
          </div>
        </CacunContainer>
      </motion.div>

      {/* User Reviews */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.4 }}
      >
        <CacunContainer>
          <h2 className="text-xl font-bold text-text mb-4">Partner Reviews</h2>
          <div className="space-y-4">
            <div className="border-l-4 border-primary/30 pl-4">
              <div className="flex items-center justify-between mb-2">
                <h4 className="font-semibold text-text">Sarah Chen</h4>
                <div className="flex items-center space-x-1">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star key={star} className="w-4 h-4 fill-primary text-primary" />
                  ))}
                </div>
              </div>
              <div className="space-y-2 text-sm">
                <div className="flex items-center space-x-2">
                  <span className="text-text-muted">Behavior:</span>
                  <div className="flex space-x-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star key={star} className="w-3 h-3 fill-primary text-primary" />
                    ))}
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-text-muted">Problem Solving:</span>
                  <div className="flex space-x-1">
                    {[1, 2, 3, 4].map((star) => (
                      <Star key={star} className="w-3 h-3 fill-primary text-primary" />
                    ))}
                    <Star className="w-3 h-3 text-primary/30" />
                  </div>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-text-muted">Vibe Check:</span>
                  <div className="flex space-x-1">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star key={star} className="w-3 h-3 fill-primary text-primary" />
                    ))}
                  </div>
                </div>
              </div>
              <p className="text-text-muted text-sm mt-2">
                "Amazing travel partner! Very organized and great at finding local experiences."
              </p>
            </div>
          </div>
        </CacunContainer>
      </motion.div>
    </div>
  );
}
