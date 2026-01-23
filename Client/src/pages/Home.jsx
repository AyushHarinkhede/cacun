import { useState } from 'react';
import { Heart, MessageCircle, Share2, Star, ChevronLeft, ChevronRight } from 'lucide-react';

export default function Home() {
  const [currentSlide, setCurrentSlide] = useState(0);
  
  const tripHighlights = [
    { id: 1, image: '/kayaking.jpg', alt: 'People kayaking' },
    { id: 2, image: '/boat-lake.jpg', alt: 'Boat on lake' },
    { id: 3, image: '/water-walk.jpg', alt: 'Person walking by water' },
  ];

  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % tripHighlights.length);
  };

  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + tripHighlights.length) % tripHighlights.length);
  };

  return (
    <div className="min-h-screen bg-background pb-28">
      <div className="max-w-7xl mx-auto px-4 py-6 space-y-6">
        
        {/* First Post - Cacun */}
        <div className="post-card slide-in-up" style={{ animationDelay: '0.1s' }}>
          <div className="post-header">
            <div className="post-avatar">
              <span className="text-primary font-semibold">C</span>
            </div>
            <div>
              <h3 className="text-text font-semibold">cacun</h3>
              <p className="text-text-muted text-sm">2 months ago</p>
            </div>
          </div>

          <div className="post-content">
            <p className="text-text mb-3">We lore free trip in Varais</p>
            
            <div className="post-image mb-3">
              <img 
                src="/cliff-water.jpg" 
                alt="Person on cliff overlooking water"
                className="w-full h-48 object-cover rounded-lg"
              />
            </div>
            
            <div className="post-engagement">
              <p className="text-text-muted text-sm mb-2">Liked by recull smiocteon</p>
              <div className="flex items-center space-x-6">
                <button className="engagement-button">
                  <Heart className="w-5 h-5" />
                  <span className="text-sm">4</span>
                </button>
                <button className="engagement-button">
                  <MessageCircle className="w-5 h-5" />
                  <span className="text-sm">0</span>
                </button>
                <button className="engagement-button">
                  <Share2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Second Post - Cacun */}
        <div className="post-card slide-in-up" style={{ animationDelay: '0.2s' }}>
          <div className="post-header">
            <div className="post-avatar">
              <span className="text-primary font-semibold">C</span>
            </div>
            <div>
              <h3 className="text-text font-semibold">cacun</h3>
              <p className="text-text-muted text-sm">2 minutes ago</p>
            </div>
          </div>

          <div className="post-content">
            <p className="text-text">Which could surny</p>
            
            <div className="post-engagement mt-3">
              <div className="flex items-center space-x-6">
                <button className="engagement-button">
                  <Heart className="w-5 h-5" />
                  <span className="text-sm">2</span>
                </button>
                <button className="engagement-button">
                  <MessageCircle className="w-5 h-5" />
                  <span className="text-sm">0</span>
                </button>
                <button className="engagement-button">
                  <Share2 className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Trip Highlights */}
        <div className="cacun-container slide-in-up" style={{ animationDelay: '0.3s' }}>
          <h2 className="text-xl font-bold text-text mb-4">Trip Highlights</h2>
          
          <div className="carousel-container">
            <div className="carousel-track" style={{ transform: `translateX(-${currentSlide * 100}%)` }}>
              {tripHighlights.map((slide) => (
                <div key={slide.id} className="carousel-slide">
                  <img 
                    src={slide.image} 
                    alt={slide.alt}
                    className="w-full h-48 object-cover rounded-lg"
                  />
                </div>
              ))}
            </div>
            
            <button 
              onClick={prevSlide}
              className="absolute left-2 top-1/2 transform -translate-y-1/2 bg-primary/80 text-white p-2 rounded-full hover:bg-primary transition"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <button 
              onClick={nextSlide}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 bg-primary/80 text-white p-2 rounded-full hover:bg-primary transition"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
          
          <div className="carousel-dots">
            {tripHighlights.map((_, index) => (
              <button
                key={index}
                onClick={() => setCurrentSlide(index)}
                className={`carousel-dot ${index === currentSlide ? 'active' : ''}`}
              />
            ))}
          </div>
        </div>

        {/* Partner Reviews */}
        <div className="cacun-container slide-in-up" style={{ animationDelay: '0.4s' }}>
          <h2 className="text-xl font-bold text-text mb-4">Partner Reviews</h2>
          
          <div className="space-y-4">
            <div className="review-card">
              <div className="review-header">
                <h4 className="font-semibold text-text">Rosa</h4>
                <div className="star-rating">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star key={star} className="w-4 h-4 fill-primary text-primary" />
                  ))}
                </div>
              </div>
              <p className="text-text-muted text-sm">
                sivernitic Uriwht Defiore tilemdrides Laver experience
              </p>
            </div>
            
            <div className="review-card">
              <div className="review-header">
                <h4 className="font-semibold text-text">Mar</h4>
                <div className="star-rating">
                  {[1, 2, 3, 4].map((star) => (
                    <Star key={star} className="w-4 h-4 fill-primary text-primary" />
                  ))}
                  <Star className="w-4 h-4 text-primary/30" />
                </div>
              </div>
              <p className="text-text-muted text-sm">
                Great travel companion!
              </p>
            </div>
          </div>
        </div>

        {/* User Profile Card */}
        <div className="cacun-container slide-in-up" style={{ animationDelay: '0.5s' }}>
          <div className="profile-card">
            <div className="profile-avatar">
              <img 
                src="/profile-avatar.jpg" 
                alt="Jomnhaes"
                className="w-16 h-16 rounded-full object-cover"
              />
            </div>
            <div className="profile-info">
              <h3 className="text-lg font-bold text-text">Jomnhaes</h3>
              <p className="text-text-muted text-sm">11 months ago</p>
              <div className="profile-stats">
                <span className="text-text-muted">Reputation: </span>
                <span className="text-primary font-semibold">132</span>
              </div>
            </div>
          </div>
        </div>

        {/* Find Travel Partner */}
        <div className="cacun-container slide-in-up" style={{ animationDelay: '0.6s' }}>
          <h2 className="text-xl font-bold text-text mb-4">Find Travel Partner</h2>
          
          <div className="search-form">
            <div className="form-grid">
              <select className="form-select">
                <option>All</option>
                <option>Adventure</option>
                <option>Relaxation</option>
                <option>Cultural</option>
              </select>
              
              <select className="form-select">
                <option>All Time</option>
                <option>This Week</option>
                <option>This Month</option>
                <option>This Year</option>
              </select>
              
              <select className="form-select">
                <option>All time Bnd</option>
                <option>North America</option>
                <option>Europe</option>
                <option>Asia</option>
              </select>
            </div>
            
            <button className="find-button">
              Find
            </button>
          </div>
        </div>

      </div>
    </div>
  );
}
