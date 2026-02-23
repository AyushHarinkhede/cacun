import { useState, useEffect } from 'react';
import { Calendar, MapPin, DollarSign, AlertCircle, Check, Users, Clock, Tag } from 'lucide-react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function CreateTrip() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    destination: '',
    startDate: '',
    endDate: '',
    budget: 1500,
    description: '',
    vibeTags: [],
    maxTravelers: 2,
    accommodation: 'any',
    transportation: 'any'
  });
  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);
  const [suggestions, setSuggestions] = useState([]);

  const vibeTags = [
    'Chill', 'Adventure', 'Party', 'Cultural', 'Nature', 
    'Foodie', 'Photography', 'Shopping', 'Nightlife', 'Spiritual',
    'Budget-friendly', 'Luxury', 'Backpacking', 'Family-friendly', 'Solo travel'
  ];
  
  const accommodations = ['any', 'hotel', 'hostel', 'airbnb', 'resort', 'camping'];
  const transportation = ['any', 'flight', 'train', 'bus', 'car', 'cruise'];
  
  // Destination suggestions
  const popularDestinations = [
    'Bali, Indonesia', 'Paris, France', 'Tokyo, Japan', 'New York, USA',
    'Dubai, UAE', 'London, UK', 'Barcelona, Spain', 'Rome, Italy',
    'Sydney, Australia', 'Bangkok, Thailand', 'Istanbul, Turkey', 'Amsterdam, Netherlands'
  ];

  const handleVibeTagToggle = (tag) => {
    setFormData(prev => ({
      ...prev,
      vibeTags: prev.vibeTags.includes(tag)
        ? prev.vibeTags.filter(t => t !== tag)
        : [...prev.vibeTags, tag]
    }));
  };
  
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.destination.trim()) {
      newErrors.destination = 'Destination is required';
    }
    
    if (!formData.startDate) {
      newErrors.startDate = 'Start date is required';
    }
    
    if (!formData.endDate) {
      newErrors.endDate = 'End date is required';
    }
    
    if (formData.startDate && formData.endDate && new Date(formData.startDate) >= new Date(formData.endDate)) {
      newErrors.endDate = 'End date must be after start date';
    }
    
    if (formData.startDate && new Date(formData.startDate) < new Date().setHours(0,0,0,0)) {
      newErrors.startDate = 'Start date cannot be in the past';
    }
    
    if (!formData.description.trim()) {
      newErrors.description = 'Description is required';
    } else if (formData.description.trim().length < 20) {
      newErrors.description = 'Description must be at least 20 characters';
    }
    
    if (formData.vibeTags.length === 0) {
      newErrors.vibeTags = 'Select at least one vibe tag';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  
  const handleDestinationChange = (e) => {
    const value = e.target.value;
    setFormData(prev => ({ ...prev, destination: value }));
    
    // Show suggestions
    if (value.length > 2) {
      const filtered = popularDestinations.filter(dest => 
        dest.toLowerCase().includes(value.toLowerCase())
      );
      setSuggestions(filtered.slice(0, 5));
    } else {
      setSuggestions([]);
    }
  };
  
  const selectSuggestion = (destination) => {
    setFormData(prev => ({ ...prev, destination }));
    setSuggestions([]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      const payload = {
        ...formData,
        budget: parseInt(formData.budget),
        maxTravelers: parseInt(formData.maxTravelers),
        createdAt: new Date().toISOString()
      };

      const response = await axios.post('/api/trips', payload);
      
      // Show success state
      setShowSuccess(true);
      
      // Reset form after 2 seconds
      setTimeout(() => {
        setFormData({
          destination: '',
          startDate: '',
          endDate: '',
          budget: 1500,
          description: '',
          vibeTags: [],
          maxTravelers: 2,
          accommodation: 'any',
          transportation: 'any'
        });
        setShowSuccess(false);
        navigate('/');
      }, 2000);
      
    } catch (err) {
      const message = err?.response?.data?.error || err?.message || 'Failed to create trip';
      setErrors({ submit: message });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <div className="slide-in-up">
        <h1 className="text-3xl font-bold text-text mb-2">Create Your Trip</h1>
        <p className="text-text-muted mb-8">
          Share your travel plans and find the perfect travel buddies
        </p>

        <form onSubmit={handleSubmit} className="create-trip-form space-y-6">
          {/* Destination */}
          <div className="form-group">
            <label className="form-label">
              <MapPin className="inline w-4 h-4 mr-2" />
              Destination Name *
            </label>
            <div className="relative">
              <input
                type="text"
                placeholder="e.g., Bali, Indonesia"
                value={formData.destination}
                onChange={handleDestinationChange}
                className={`form-input ${errors.destination ? 'border-red-500' : ''}`}
                required
              />
              {suggestions.length > 0 && (
                <div className="absolute top-full left-0 right-0 mt-1 bg-background border border-primary/20 rounded-lg shadow-lg z-10">
                  {suggestions.map((suggestion, index) => (
                    <button
                      key={index}
                      type="button"
                      onClick={() => selectSuggestion(suggestion)}
                      className="w-full px-4 py-2 text-left hover:bg-primary/10 first:rounded-t-lg last:rounded-b-lg"
                    >
                      <MapPin className="inline w-3 h-3 mr-2 text-primary/60" />
                      {suggestion}
                    </button>
                  ))}
                </div>
              )}
            </div>
            {errors.destination && (
              <p className="text-red-400 text-sm mt-1 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.destination}
              </p>
            )}
          </div>

          {/* Date Range */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">
                <Calendar className="inline w-4 h-4 mr-2" />
                Start Date *
              </label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) => setFormData(prev => ({ ...prev, startDate: e.target.value }))}
                min={new Date().toISOString().split('T')[0]}
                className={`form-input ${errors.startDate ? 'border-red-500' : ''}`}
                required
              />
              {errors.startDate && (
                <p className="text-red-400 text-sm mt-1 flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" />
                  {errors.startDate}
                </p>
              )}
            </div>
            <div className="form-group">
              <label className="form-label">
                <Calendar className="inline w-4 h-4 mr-2" />
                End Date *
              </label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => setFormData(prev => ({ ...prev, endDate: e.target.value }))}
                min={formData.startDate || new Date().toISOString().split('T')[0]}
                className={`form-input ${errors.endDate ? 'border-red-500' : ''}`}
                required
              />
              {errors.endDate && (
                <p className="text-red-400 text-sm mt-1 flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" />
                  {errors.endDate}
                </p>
              )}
            </div>
          </div>

          {/* Trip Duration */}
          {formData.startDate && formData.endDate && (
            <div className="bg-primary/10 rounded-lg p-3 flex items-center gap-2">
              <Clock className="w-4 h-4 text-primary" />
              <span className="text-sm text-text">
                Trip Duration: {Math.ceil((new Date(formData.endDate) - new Date(formData.startDate)) / (1000 * 60 * 60 * 24))} days
              </span>
            </div>
          )}

          {/* Max Travelers */}
          <div className="form-group">
            <label className="form-label">
              <Users className="inline w-4 h-4 mr-2" />
              Max Travelers: {formData.maxTravelers}
            </label>
            <input
              type="range"
              min="1"
              max="10"
              value={formData.maxTravelers}
              onChange={(e) => setFormData(prev => ({ ...prev, maxTravelers: parseInt(e.target.value) }))}
              className="w-full h-2 bg-card rounded-lg appearance-none cursor-pointer"
            />
            <div className="flex justify-between text-xs text-text-muted mt-1">
              <span>Solo</span>
              <span>Group</span>
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

          {/* Accommodation & Transportation */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="form-group">
              <label className="form-label">Accommodation</label>
              <select
                value={formData.accommodation}
                onChange={(e) => setFormData(prev => ({ ...prev, accommodation: e.target.value }))}
                className="form-input"
              >
                {accommodations.map(acc => (
                  <option key={acc} value={acc}>
                    {acc.charAt(0).toUpperCase() + acc.slice(1)}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label className="form-label">Transportation</label>
              <select
                value={formData.transportation}
                onChange={(e) => setFormData(prev => ({ ...prev, transportation: e.target.value }))}
                className="form-input"
              >
                {transportation.map(trans => (
                  <option key={trans} value={trans}>
                    {trans.charAt(0).toUpperCase() + trans.slice(1)}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Trip Description */}
          <div className="form-group">
            <label className="form-label">Trip Plan & Description *</label>
            <textarea
              placeholder="Describe your trip plans, activities, and what you're looking for in travel buddies..."
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              className={`form-textarea ${errors.description ? 'border-red-500' : ''}`}
              rows="4"
              required
            />
            <div className="flex justify-between items-center mt-1">
              <span className="text-xs text-text-muted">
                {formData.description.length}/500 characters
              </span>
              {errors.description && (
                <p className="text-red-400 text-sm flex items-center gap-1">
                  <AlertCircle className="w-4 h-4" />
                  {errors.description}
                </p>
              )}
            </div>
          </div>

          {/* Vibe Tags */}
          <div className="form-group">
            <label className="form-label">
              <Tag className="inline w-4 h-4 mr-2" />
              Trip Vibe Tags *
            </label>
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
            {errors.vibeTags && (
              <p className="text-red-400 text-sm mt-1 flex items-center gap-1">
                <AlertCircle className="w-4 h-4" />
                {errors.vibeTags}
              </p>
            )}
          </div>

          {/* Error Message */}
          {errors.submit && (
            <div className="bg-red-500/10 border border-red-500/20 rounded-lg p-3 text-red-400 text-sm flex items-center gap-2">
              <AlertCircle className="w-4 h-4" />
              {errors.submit}
            </div>
          )}

          {/* Success Message */}
          {showSuccess && (
            <div className="bg-green-500/10 border border-green-500/20 rounded-lg p-4 text-green-400 flex items-center gap-3">
              <Check className="w-6 h-6" />
              <div>
                <h4 className="font-semibold">Trip Created Successfully!</h4>
                <p className="text-sm">Your trip has been posted and other travelers can now find it.</p>
              </div>
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isSubmitting || showSuccess}
            className={`request-button text-lg py-4 flex items-center justify-center gap-2 ${
              isSubmitting || showSuccess ? 'opacity-50 cursor-not-allowed' : ''
            }`}
          >
            {isSubmitting ? (
              <>
                <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                Creating Trip...
              </>
            ) : showSuccess ? (
              <>
                <Check className="w-5 h-5" />
                Success!
              </>
            ) : (
              'Create Trip'
            )}
          </button>
        </form>
      </div>
    </div>
  );
}
