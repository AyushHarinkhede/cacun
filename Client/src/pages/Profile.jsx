import { motion, AnimatePresence } from 'framer-motion';
import { useState, useRef, useEffect } from 'react';
import CacunContainer from '../components/CacunContainer';
import { 
  Star, MapPin, Calendar, Award, Users, Shield, LogOut, Edit, Eye, EyeOff, 
  Mail, Lock, User, Camera, Settings, X, Check, AlertCircle, Upload
} from 'lucide-react';

// Simple in-memory auth store
let authUser = JSON.parse(localStorage.getItem('cacun_user') || 'null');

function setAuthUser(user) {
  authUser = user;
  if (user) localStorage.setItem('cacun_user', JSON.stringify(user));
  else localStorage.removeItem('cacun_user');
}

// User profile data store
let userProfile = JSON.parse(localStorage.getItem('cacun_profile') || '{}');

function setUserProfile(profile) {
  userProfile = { ...userProfile, ...profile };
  localStorage.setItem('cacun_profile', JSON.stringify(userProfile));
}

export default function Profile() {
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [form, setForm] = useState({ email: '', password: '', name: '', rememberMe: false });
  const [error, setError] = useState('');
  const [user, setUser] = useState(authUser);
  const [isEditing, setIsEditing] = useState(false);
  const [showLogoutDialog, setShowLogoutDialog] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const fileInputRef = useRef(null);
  
  // Profile form state
  const [profileForm, setProfileForm] = useState({
    name: userProfile.name || user?.name || '',
    bio: userProfile.bio || 'Adventure seeker and culture enthusiast! Love exploring off-the-beaten-path destinations and meeting fellow travelers. I\'m organized, respectful of local customs, and always up for trying new foods. Looking for travel buddies who share similar values!',
    location: userProfile.location || 'San Francisco, CA',
    dob: userProfile.dob || '1995-06-15',
    gender: userProfile.gender || 'prefer-not-to-say',
    email: userProfile.email || user?.email || '',
    username: userProfile.username || user?.name?.toLowerCase() || '',
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  
  // Settings state
  const [settings, setSettings] = useState({
    incognito: false,
    textSize: 'medium',
    darkMode: false,
    notifications: true,
    locationSharing: true,
    showOnlineStatus: true
  });

  const handleAuth = (e) => {
    e.preventDefault();
    setError('');
    setSuccessMessage('');
    
    // Form validation
    if (!form.email || !form.password || (!isLogin && !form.name)) {
      setError('Please fill all required fields');
      return;
    }
    
    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(form.email)) {
      setError('Please enter a valid email address');
      return;
    }
    
    // Password validation
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters long');
      return;
    }
    
    // Simulate API call
    setTimeout(() => {
      const newUser = {
        id: Date.now(),
        email: form.email,
        name: form.name || form.email.split('@')[0],
        avatar: (form.name || form.email)[0].toUpperCase(),
        token: 'mock-jwt-token-' + Date.now()
      };
      setAuthUser(newUser);
      setUser(newUser);
      
      if (form.rememberMe) {
        localStorage.setItem('cacun_remember', 'true');
      }
      
      setForm({ email: '', password: '', name: '', rememberMe: false });
      setSuccessMessage(isLogin ? 'Login successful!' : 'Account created successfully!');
      setTimeout(() => setSuccessMessage(''), 3000);
    }, 1000);
  };
  
  const handleOAuthLogin = (provider) => {
    // Simulate OAuth flow
    const providerNames = {
      google: 'Google',
      facebook: 'Facebook',
      apple: 'Apple',
      microsoft: 'Microsoft'
    };
    
    setSuccessMessage(`Connecting to ${providerNames[provider]}...`);
    
    // Simulate OAuth popup
    setTimeout(() => {
      const oauthUser = {
        id: Date.now(),
        email: `user@${provider}.com`,
        name: `${providerNames[provider]} User`,
        avatar: providerNames[provider][0],
        token: 'oauth-token-' + Date.now()
      };
      setAuthUser(oauthUser);
      setUser(oauthUser);
      setSuccessMessage(`${providerNames[provider]} login successful!`);
      setTimeout(() => setSuccessMessage(''), 3000);
    }, 1500);
  };

  const handleLogout = () => {
    setAuthUser(null);
    setUser(null);
    setShowLogoutDialog(false);
  };
  
  const handleProfileSave = () => {
    // Validation
    if (!profileForm.name || !profileForm.email) {
      setError('Name and email are required');
      return;
    }
    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(profileForm.email)) {
      setError('Please enter a valid email address');
      return;
    }
    
    // Password validation if changing password
    if (profileForm.newPassword) {
      if (profileForm.newPassword.length < 6) {
        setError('New password must be at least 6 characters long');
        return;
      }
      if (profileForm.newPassword !== profileForm.confirmPassword) {
        setError('Passwords do not match');
        return;
      }
    }
    
    // Save profile
    setUserProfile(profileForm);
    setUser(prev => ({ ...prev, name: profileForm.name, email: profileForm.email }));
    setSuccessMessage('Profile updated successfully!');
    setError('');
    setTimeout(() => setSuccessMessage(''), 3000);
  };
  
  const handleProfilePictureUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      // In a real app, upload to server
      const reader = new FileReader();
      reader.onloadend = () => {
        setUserProfile({ profilePicture: reader.result });
        setSuccessMessage('Profile picture updated!');
        setTimeout(() => setSuccessMessage(''), 3000);
      };
      reader.readAsDataURL(file);
    }
  };
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

  if (!user) {
    return (
      <div className="max-w-md mx-auto space-y-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <CacunContainer>
            <h2 className="text-2xl font-bold text-center text-text mb-6">
              {isLogin ? 'Login to Cacun' : 'Join Cacun'}
            </h2>
            <form onSubmit={handleAuth} className="space-y-4">
              {!isLogin && (
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Name</label>
                  <div className="relative">
                    <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-primary/60" />
                    <input
                      type="text"
                      value={form.name}
                      onChange={(e) => setForm({ ...form, name: e.target.value })}
                      placeholder="Your name"
                      className="w-full pl-10 pr-4 py-3 rounded-xl bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                    />
                  </div>
                </div>
              )}
              <div>
                <label className="block text-sm font-medium text-text mb-1">Email</label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-primary/60" />
                  <input
                    type="email"
                    value={form.email}
                    onChange={(e) => setForm({ ...form, email: e.target.value })}
                    placeholder="you@example.com"
                    className="w-full pl-10 pr-4 py-3 rounded-xl bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-text mb-1">Password</label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-primary/60" />
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={form.password}
                    onChange={(e) => setForm({ ...form, password: e.target.value })}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-12 py-3 rounded-xl bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-primary/60 hover:text-primary"
                  >
                    {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                  </button>
                </div>
              </div>
              {error && (
                <div className="text-red-400 text-sm text-center flex items-center justify-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  {error}
                </div>
              )}
              {successMessage && (
                <div className="text-green-400 text-sm text-center flex items-center justify-center gap-2">
                  <Check className="w-4 h-4" />
                  {successMessage}
                </div>
              )}
              <button
                type="submit"
                className="w-full bg-primary hover:opacity-90 text-white py-3 rounded-xl font-semibold transition"
              >
                {isLogin ? 'Login' : 'Sign Up'}
              </button>
            </form>

            {/* OAuth Buttons */}
            <div className="mt-6">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-primary/20"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-background text-text-muted">Or continue with</span>
                </div>
              </div>
              
              <div className="grid grid-cols-2 gap-3 mt-4">
                <button
                  onClick={() => handleOAuthLogin('google')}
                  className="flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                    <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                    <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                    <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                  </svg>
                  <span className="text-gray-700 font-medium">Google</span>
                </button>
                <button
                  onClick={() => handleOAuthLogin('microsoft')}
                  className="flex items-center justify-center gap-2 px-4 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
                >
                  <svg className="w-5 h-5" viewBox="0 0 24 24">
                    <path fill="#F25022" d="M11.4 11.4H2.6V2.6h8.8v8.8z"/>
                    <path fill="#7FBA00" d="M21.4 11.4h-8.8V2.6h8.8v8.8z"/>
                    <path fill="#00A4EF" d="M11.4 21.4H2.6v-8.8h8.8v8.8z"/>
                    <path fill="#FFB900" d="M21.4 21.4h-8.8v-8.8h8.8v8.8z"/>
                  </svg>
                  <span className="text-gray-700 font-medium">Microsoft</span>
                </button>
                <button
                  onClick={() => handleOAuthLogin('apple')}
                  className="flex items-center justify-center gap-2 px-4 py-3 bg-black hover:bg-gray-800 rounded-lg transition"
                >
                  <svg className="w-5 h-5 text-white" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M18.71 19.5c-.83 1.24-1.71 2.45-3.05 2.47-1.34.03-1.77-.79-3.29-.79-1.53 0-2 .77-3.27.82-1.31.05-2.3-1.32-3.14-2.53C4.25 17 2.94 12.45 4.7 9.39c.87-1.52 2.43-2.48 4.12-2.51 1.28-.02 2.5.87 3.29.87.78 0 2.26-1.07 3.81-.91.65.03 2.47.26 3.64 1.98-.09.06-2.17 1.28-2.15 3.81.03 3.02 2.65 4.03 2.68 4.04-.03.07-.42 1.44-1.38 2.83M13 3.5c.73-.83 1.94-1.46 2.94-1.5.13 1.17-.34 2.35-1.04 3.19-.69.85-1.83 1.51-2.95 1.42-.15-1.15.41-2.35 1.05-3.11z"/>
                  </svg>
                  <span className="text-white font-medium">Apple</span>
                </button>
                <button
                  onClick={() => handleOAuthLogin('facebook')}
                  className="flex items-center justify-center gap-2 px-4 py-3 bg-[#1877F2] hover:bg-[#166FE5] rounded-lg transition"
                >
                  <svg className="w-5 h-5 text-white" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                  </svg>
                  <span className="text-white font-medium">Facebook</span>
                </button>
              </div>
            </div>

            {/* Remember Me */}
            {isLogin && (
              <div className="mt-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.rememberMe}
                    onChange={(e) => setForm({ ...form, rememberMe: e.target.checked })}
                    className="w-4 h-4 text-primary border-primary/20 rounded focus:ring-primary"
                  />
                  <span className="text-sm text-text">Remember me</span>
                </label>
              </div>
            )}
            <div className="text-center mt-6">
              <button
                type="button"
                onClick={() => setIsLogin(!isLogin)}
                className="text-primary hover:underline text-sm"
              >
                {isLogin ? "Don't have an account? Sign up" : 'Already have an account? Login'}
              </button>
            </div>
          </CacunContainer>
        </motion.div>
      </div>
    );
  }

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
              <span className="text-primary text-2xl font-bold">{user.avatar}</span>
            </div>
            <div className="flex-1">
              <h2 className="text-2xl font-bold text-text">{userProfile.name || user.name}</h2>
              <p className="text-text-muted">@{userProfile.username || user.name.toLowerCase()} • Travel Enthusiast</p>
              <div className="flex items-center space-x-4 mt-2 text-sm text-text-muted">
                <span className="flex items-center space-x-1">
                  <MapPin className="w-4 h-4" />
                  <span>{userProfile.location || 'San Francisco, CA'}</span>
                </span>
                <span className="flex items-center space-x-1">
                  <Calendar className="w-4 h-4" />
                  <span>Joined Jan 2023</span>
                </span>
              </div>
            </div>
            <div className="flex gap-2">
              <button 
                onClick={() => setIsEditing(true)}
                className="px-4 py-2 bg-primary/20 hover:bg-primary/30 text-primary rounded-lg font-semibold transition flex items-center gap-2"
              >
                <Edit className="w-4 h-4" />
                Edit Profile
              </button>
              <button 
                onClick={() => setShowSettings(true)}
                className="px-4 py-2 bg-primary/20 hover:bg-primary/30 text-primary rounded-lg font-semibold transition flex items-center gap-2"
              >
                <Settings className="w-4 h-4" />
                Settings
              </button>
              <button
                onClick={() => setShowLogoutDialog(true)}
                className="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 text-red-400 rounded-lg font-semibold transition flex items-center gap-2"
              >
                <LogOut className="w-4 h-4" />
                Logout
              </button>
            </div>
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
            {userProfile.bio || 'Adventure seeker and culture enthusiast! Love exploring off-the-beaten-path destinations and meeting fellow travelers. I\'m organized, respectful of local customs, and always up for trying new foods. Looking for travel buddies who share similar values!'}
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

      {/* Edit Profile Modal */}
      <AnimatePresence>
        {isEditing && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm flex items-center justify-center z-[60] p-4"
            onClick={() => setIsEditing(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-background rounded-xl p-6 max-w-2xl w-full max-h-[90vh] overflow-y-auto mb-20"
            >
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-bold text-text">Edit Profile</h3>
                <button
                  onClick={() => setIsEditing(false)}
                  className="p-2 hover:bg-primary/10 rounded-lg"
                >
                  <X className="w-5 h-5 text-text" />
                </button>
              </div>

              {/* Profile Picture */}
              <div className="flex flex-col items-center mb-6">
                <div className="relative">
                  <div className="w-24 h-24 rounded-full bg-primary/20 flex items-center justify-center">
                    {userProfile.profilePicture ? (
                      <img src={userProfile.profilePicture} alt="Profile" className="w-full h-full rounded-full object-cover" />
                    ) : (
                      <span className="text-primary text-3xl font-bold">{user?.avatar}</span>
                    )}
                  </div>
                  <button
                    onClick={() => fileInputRef.current?.click()}
                    className="absolute bottom-0 right-0 p-2 bg-primary text-white rounded-full hover:bg-primary/90"
                  >
                    <Camera className="w-4 h-4" />
                  </button>
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleProfilePictureUpload}
                    className="hidden"
                  />
                </div>
                <p className="text-sm text-text-muted mt-2">Click camera to change photo</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {/* Name */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Name *</label>
                  <input
                    type="text"
                    value={profileForm.name}
                    onChange={(e) => setProfileForm({ ...profileForm, name: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                    placeholder="Your name"
                  />
                </div>

                {/* Username */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Username</label>
                  <input
                    type="text"
                    value={profileForm.username}
                    onChange={(e) => setProfileForm({ ...profileForm, username: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                    placeholder="@username"
                  />
                </div>

                {/* Email */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Email *</label>
                  <input
                    type="email"
                    value={profileForm.email}
                    onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                    placeholder="you@example.com"
                  />
                </div>

                {/* Location */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Location</label>
                  <input
                    type="text"
                    value={profileForm.location}
                    onChange={(e) => setProfileForm({ ...profileForm, location: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                    placeholder="City, Country"
                  />
                </div>

                {/* DOB */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Date of Birth</label>
                  <input
                    type="date"
                    value={profileForm.dob}
                    onChange={(e) => setProfileForm({ ...profileForm, dob: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                  />
                </div>

                {/* Gender */}
                <div>
                  <label className="block text-sm font-medium text-text mb-1">Gender</label>
                  <select
                    value={profileForm.gender}
                    onChange={(e) => setProfileForm({ ...profileForm, gender: e.target.value })}
                    className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                  >
                    <option value="prefer-not-to-say">Prefer not to say</option>
                    <option value="male">Male</option>
                    <option value="female">Female</option>
                    <option value="other">Other</option>
                  </select>
                </div>
              </div>

              {/* Bio */}
              <div className="mt-4">
                <label className="block text-sm font-medium text-text mb-1">Bio</label>
                <textarea
                  value={profileForm.bio}
                  onChange={(e) => setProfileForm({ ...profileForm, bio: e.target.value })}
                  className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                  rows="3"
                  placeholder="Tell us about yourself..."
                />
              </div>

              {/* Password Section */}
              <div className="mt-6 pt-6 border-t border-primary/20">
                <h4 className="font-semibold text-text mb-4">Change Password</h4>
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-text mb-1">Current Password</label>
                    <input
                      type="password"
                      value={profileForm.currentPassword}
                      onChange={(e) => setProfileForm({ ...profileForm, currentPassword: e.target.value })}
                      className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                      placeholder="Enter current password"
                    />
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-text mb-1">New Password</label>
                      <input
                        type="password"
                        value={profileForm.newPassword}
                        onChange={(e) => setProfileForm({ ...profileForm, newPassword: e.target.value })}
                        className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                        placeholder="New password"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-text mb-1">Confirm New Password</label>
                      <input
                        type="password"
                        value={profileForm.confirmPassword}
                        onChange={(e) => setProfileForm({ ...profileForm, confirmPassword: e.target.value })}
                        className="w-full px-4 py-2 rounded-lg bg-background/40 border border-white/10 text-text placeholder-text-muted focus:outline-none focus:border-primary/60"
                        placeholder="Confirm new password"
                      />
                    </div>
                  </div>
                </div>
              </div>

              {/* Error/Success Messages */}
              {error && (
                <div className="mt-4 text-red-400 text-sm flex items-center gap-2">
                  <AlertCircle className="w-4 h-4" />
                  {error}
                </div>
              )}
              {successMessage && (
                <div className="mt-4 text-green-400 text-sm flex items-center gap-2">
                  <Check className="w-4 h-4" />
                  {successMessage}
                </div>
              )}

              {/* Actions */}
              <div className="flex space-x-3 mt-6">
                <button
                  onClick={() => setIsEditing(false)}
                  className="flex-1 px-4 py-2 bg-primary/20 hover:bg-primary/30 text-primary rounded-lg font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  onClick={handleProfileSave}
                  className="flex-1 px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-lg font-semibold transition"
                >
                  Save Changes
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Settings Modal */}
      <AnimatePresence>
        {showSettings && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60] p-4"
            onClick={() => setShowSettings(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-background rounded-xl p-6 max-w-md w-full mb-20"
            >
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-bold text-text">Settings</h3>
                <button
                  onClick={() => setShowSettings(false)}
                  className="p-2 hover:bg-primary/10 rounded-lg"
                >
                  <X className="w-5 h-5 text-text" />
                </button>
              </div>

              <div className="space-y-6">
                {/* Incognito Mode */}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-text">Incognito Mode</h4>
                    <p className="text-sm text-text-muted">Browse privately</p>
                  </div>
                  <button
                    onClick={() => setSettings({ ...settings, incognito: !settings.incognito })}
                    className={`w-12 h-6 rounded-full transition-colors ${
                      settings.incognito ? 'bg-primary' : 'bg-primary/20'
                    }`}
                  >
                    <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                      settings.incognito ? 'translate-x-6' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>

                {/* Text Size */}
                <div>
                  <h4 className="font-medium text-text mb-3">Text Size</h4>
                  <div className="flex space-x-2">
                    {['small', 'medium', 'large'].map((size) => (
                      <button
                        key={size}
                        onClick={() => setSettings({ ...settings, textSize: size })}
                        className={`flex-1 py-2 px-3 rounded-lg capitalize transition ${
                          settings.textSize === size
                            ? 'bg-primary text-white'
                            : 'bg-primary/20 text-primary hover:bg-primary/30'
                        }`}
                      >
                        {size}
                      </button>
                    ))}
                  </div>
                </div>

                {/* Dark Mode */}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-text">Dark Mode</h4>
                    <p className="text-sm text-text-muted">Toggle dark theme</p>
                  </div>
                  <button
                    onClick={() => setSettings({ ...settings, darkMode: !settings.darkMode })}
                    className={`w-12 h-6 rounded-full transition-colors ${
                      settings.darkMode ? 'bg-primary' : 'bg-primary/20'
                    }`}
                  >
                    <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                      settings.darkMode ? 'translate-x-6' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>

                {/* Notifications */}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-text">Notifications</h4>
                    <p className="text-sm text-text-muted">Push notifications</p>
                  </div>
                  <button
                    onClick={() => setSettings({ ...settings, notifications: !settings.notifications })}
                    className={`w-12 h-6 rounded-full transition-colors ${
                      settings.notifications ? 'bg-primary' : 'bg-primary/20'
                    }`}
                  >
                    <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                      settings.notifications ? 'translate-x-6' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>

                {/* Location Sharing */}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-text">Location Sharing</h4>
                    <p className="text-sm text-text-muted">Share your location</p>
                  </div>
                  <button
                    onClick={() => setSettings({ ...settings, locationSharing: !settings.locationSharing })}
                    className={`w-12 h-6 rounded-full transition-colors ${
                      settings.locationSharing ? 'bg-primary' : 'bg-primary/20'
                    }`}
                  >
                    <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                      settings.locationSharing ? 'translate-x-6' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>

                {/* Show Online Status */}
                <div className="flex items-center justify-between">
                  <div>
                    <h4 className="font-medium text-text">Show Online Status</h4>
                    <p className="text-sm text-text-muted">Let others see when you're online</p>
                  </div>
                  <button
                    onClick={() => setSettings({ ...settings, showOnlineStatus: !settings.showOnlineStatus })}
                    className={`w-12 h-6 rounded-full transition-colors ${
                      settings.showOnlineStatus ? 'bg-primary' : 'bg-primary/20'
                    }`}
                  >
                    <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                      settings.showOnlineStatus ? 'translate-x-6' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>
              </div>

              <button
                onClick={() => setShowSettings(false)}
                className="w-full mt-6 px-4 py-2 bg-primary hover:bg-primary/90 text-white rounded-lg font-semibold transition"
              >
                Done
              </button>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Logout Confirmation Dialog */}
      <AnimatePresence>
        {showLogoutDialog && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/50 flex items-center justify-center z-[60] p-4"
            onClick={() => setShowLogoutDialog(false)}
          >
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
              className="bg-background rounded-xl p-6 max-w-sm w-full mb-20"
            >
              <h3 className="text-lg font-semibold text-text mb-2">Confirm Logout</h3>
              <p className="text-text-muted mb-6">
                Are you sure you want to logout? You'll need to login again to access your account.
              </p>
              <div className="flex space-x-3">
                <button
                  onClick={() => setShowLogoutDialog(false)}
                  className="flex-1 px-4 py-2 bg-primary/20 hover:bg-primary/30 text-primary rounded-lg font-semibold transition"
                >
                  Cancel
                </button>
                <button
                  onClick={handleLogout}
                  className="flex-1 px-4 py-2 bg-red-500 hover:bg-red-600 text-white rounded-lg font-semibold transition"
                >
                  Logout
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
