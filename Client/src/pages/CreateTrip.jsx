import { useState } from 'react';
import { Calendar, MapPin, DollarSign } from 'lucide-react';

export default function CreateTrip() {
  const [formData, setFormData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    budget: 1500,
    description: '',
    vibeTags: []
  });

  const vibeTags = [
    'Chill', 'Adventure', 'Party', 'Cultural', 'Nature', 
    'Foodie', 'Photography', 'Shopping', 'Nightlife', 'Spiritual'
  ];

  const handleVibeTagToggle = (tag) => {
    setFormData(prev => ({
      ...prev,
      vibeTags: prev.vibeTags.includes(tag)
        ? prev.vibeTags.filter(t => t !== tag)
        : [...prev.vibeTags, tag]
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    console.log('Creating trip:', formData);
    // Handle trip creation logic here
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="slide-in-up">
        <h1 className="text-3xl font-bold text-text mb-2">Create Your Trip</h1>
        <p className="text-text-muted mb-8">
          Share your travel plans and find the perfect travel buddies
        </p>

        <form onSubmit={handleSubmit} className="create-trip-form">
          {/* Destination */}
          <div className="form-group">
            <label className="form-label">
              <MapPin className="inline w-4 h-4 mr-2" />
              Destination Name
            </label>
            <input
              type="text"
              placeholder="e.g., Bali, Indonesia"
              value={formData.destination}
              onChange={(e) => setFormData(prev => ({ ...prev, destination: e.target.value }))}
              className="form-input"
              required
            />
          </div>

          {/* Date Range */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">
                <Calendar className="inline w-4 h-4 mr-2" />
                Start Date
              </label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData(prev => ({ ...prev, startDate: e.target.value }))}
                className="form-input"
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label">
                <Calendar className="inline w-4 h-4 mr-2" />
                End Date
              </label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData(prev => ({ ...prev, endDate: e.target.value }))}
                className="form-input"
                required
              />
            </div>
          </div>

          {/* Budget Slider */}
          <div className="form-group">
            <label className="form-label">
              <DollarSign className="inline w-4 h-4 mr-2" />
              Budget per Person: ${formData.budget}
            </label>
            <input
              type="range"
              min="500"
              max="10000"
              step="100"
              value={formData.budget}
              onChange={(e) => setFormData(prev => ({ ...prev, budget: parseInt(e.target.value) }))}
              className="w-full h-2 bg-card rounded-lg appearance-none cursor-pointer"
            />
            <div className="flex justify-between text-xs text-text-muted mt-1">
              <span>$500</span>
              <span>$10,000</span>
            </div>
          </div>

          {/* Trip Description */}
          <div className="form-group">
            <label className="form-label">Trip Plan & Description</label>
            <textarea
              placeholder="Describe your trip plans, activities, and what you're looking for in travel buddies..."
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              className="form-textarea"
              rows="4"
              required
            />
          </div>

          {/* Vibe Tags */}
          <div className="form-group">
            <label className="form-label">Trip Vibe Tags</label>
            <div className="vibe-tags">
              {vibeTags.map((tag) => (
                <button
                  key={tag}
                  type="button"
                  onClick={() => handleVibeTagToggle(tag)}
                  className={`vibe-tag ${formData.vibeTags.includes(tag) ? 'selected' : ''}`}
                >
                  {tag}
                </button>
              ))}
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            className="request-button text-lg py-4"
          >
            Create Trip
          </button>
        </form>
      </div>
    </div>
  );
}
