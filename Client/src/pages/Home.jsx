import { useMemo, useRef, useState, useEffect } from 'react';
import {
  Bell,
  Bookmark,
  ChevronLeft,
  ChevronRight,
  Heart,
  MessageCircle,
  MoreVertical,
  Search,
  Send,
  Star,
  TrendingUp,
  Users,
  Clock,
  MapPin,
  Calendar,
  Filter,
  X,
  Sparkles,
  Compass,
  Globe,
  Map,
  Camera,
  Mountain,
  Waves,
  TreePine,
} from 'lucide-react';
import CacunContainer from '../components/CacunContainer';
import { useNavigate } from 'react-router-dom';
import { useTheme } from '../contexts/ThemeContext';

export default function Home() {
  const navigate = useNavigate();
  const { isDark } = useTheme();
  const [likedPost, setLikedPost] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchOpen, setSearchOpen] = useState(false);
  const [savedPosts, setSavedPosts] = useState([1, 3]);
  const [recentSearches, setRecentSearches] = useState(['beach trips', 'adventure travel', 'europe tours']);
  const [notifications, setNotifications] = useState([
    { id: 1, type: 'message', text: 'Sarah sent you a message', time: '2m ago', read: false },
    { id: 2, type: 'trip_request', text: 'John wants to join your Bali trip', time: '1h ago', read: false },
    { id: 3, type: 'review', text: 'You received a 5-star review!', time: '3h ago', read: true },
  ]);
  const [showFilters, setShowFilters] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [isSearchFocused, setIsSearchFocused] = useState(false);
  const [isBlurred, setIsBlurred] = useState(false);
  const filterRef = useRef(null);

  const highlightsRef = useRef(null);
  const reviewsRef = useRef(null);
  const findPartnerRef = useRef(null);

  const features = useMemo(
    () => [
      {
        id: 'explore',
        title: 'Explore Trips',
        description: 'Browse trips, filter by category, and request to join.',
        action: () => navigate('/explore'),
      },
      {
        id: 'create',
        title: 'Create Trip',
        description: 'Create your trip plan and invite travel partners.',
        action: () => navigate('/create'),
      },
      {
        id: 'messages',
        title: 'Messages',
        description: 'Chat with travel buddies and manage conversations.',
        action: () => navigate('/messages'),
      },
      {
        id: 'profile',
        title: 'Profile',
        description: 'View reputation, stats, and reviews.',
        action: () => navigate('/profile'),
      },
      {
        id: 'highlights',
        title: 'Trip Highlights',
        description: 'Swipe through recent highlight photos.',
        action: () => highlightsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }),
      },
      {
        id: 'reviews',
        title: 'Partner Reviews',
        description: 'See partner feedback and star ratings.',
        action: () => reviewsRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }),
      },
      {
        id: 'find',
        title: 'Find Travel Partner',
        description: 'Use filters to find the best travel match.',
        action: () => findPartnerRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' }),
      },
    ],
    [navigate]
  );

  const featureResults = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return [];
    return features
      .map((f) => {
        const hay = `${f.title} ${f.description}`.toLowerCase();
        const score = hay.includes(q) ? 1 : 0;
        return { f, score };
      })
      .filter((x) => x.score > 0)
      .map((x) => ({ ...x.f, type: 'feature', location: 'Home' }));
  }, [features, searchQuery]);

  // Enhanced search results with all types
  const allSearchResults = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return [];
    
    const results = [];
    
    // Add features
    features.forEach(f => {
      const hay = `${f.title} ${f.description}`.toLowerCase();
      if (hay.includes(q)) {
        results.push({ ...f, type: 'feature', location: 'Home' });
      }
    });
    
    // Add chat suggestions
    const chatSuggestions = [
      { id: 'chat-1', title: 'Messages', description: 'View conversations with travel partners', type: 'chat', location: 'Messages' },
      { id: 'chat-2', title: 'Travel Groups', description: 'Join group conversations', type: 'chat', location: 'Messages' },
      { id: 'chat-3', title: 'Support Chat', description: 'Get help from support team', type: 'chat', location: 'Messages' },
    ];
    
    chatSuggestions.forEach(chat => {
      const hay = `${chat.title} ${chat.description}`.toLowerCase();
      if (hay.includes(q)) {
        results.push(chat);
      }
    });
    
    // Add webapp options
    const webappOptions = [
      { id: 'option-1', title: 'Settings', description: 'Manage app preferences', type: 'option', location: 'Settings' },
      { id: 'option-2', title: 'Profile', description: 'View and edit profile', type: 'option', location: 'Profile' },
      { id: 'option-3', title: 'Notifications', description: 'See all notifications', type: 'option', location: 'Home' },
      { id: 'option-4', title: 'Bookmarks', description: 'View saved items', type: 'option', location: 'Profile' },
      { id: 'option-5', title: 'Help Center', description: 'Get help and support', type: 'option', location: 'Settings' },
      { id: 'option-6', title: 'Privacy', description: 'Manage privacy settings', type: 'option', location: 'Settings' },
    ];
    
    webappOptions.forEach(option => {
      const hay = `${option.title} ${option.description}`.toLowerCase();
      if (hay.includes(q)) {
        results.push(option);
      }
    });
    
    return results.slice(0, 8);
  }, [features, searchQuery]);

  const highlights = useMemo(
    () => [
      { id: 1, seed: 'highlight-1' },
      { id: 2, seed: 'highlight-2' },
      { id: 3, seed: 'highlight-3' },
      { id: 4, seed: 'highlight-4' },
      { id: 5, seed: 'highlight-5' },
    ],
    []
  );

  const [activeHighlight, setActiveHighlight] = useState(0);

  const reviews = useMemo(
    () => [
      {
        id: 1,
        name: 'Rosa',
        role: 'Star ratings',
        rating: 5,
        text:
          'Experience was swimming with Pedro the first experience I traveled as the tentdriss area. I experience this meant during times. I got assistant with experience it meant during times.',
      },
      {
        id: 2,
        name: 'Mark',
        role: 'Star ratings',
        rating: 5,
        text:
          'Experience was great. The crew was friendly and the vibe was amazing. Would definitely travel again with this group.',
      },
      {
        id: 3,
        name: 'Aiko',
        role: 'Star ratings',
        rating: 4,
        text:
          'Super smooth trip coordination. Good communication and respectful people. Strongly recommended for first-time travelers.',
      },
    ],
    []
  );

  const handleSavePost = (postId) => {
    setSavedPosts(prev => 
      prev.includes(postId) 
        ? prev.filter(id => id !== postId)
        : [...prev, postId]
    );
  };

  const handleSearch = (query) => {
    if (query.trim()) {
      setRecentSearches(prev => [query, ...prev.filter(s => s !== query)].slice(0, 5));
    }
  };

  const markNotificationRead = (id) => {
    setNotifications(prev => 
      prev.map(n => n.id === id ? { ...n, read: true } : n)
    );
  };

  // Close filter dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (filterRef.current && !filterRef.current.contains(event.target)) {
        setShowFilters(false);
      }
    };

    if (showFilters) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showFilters]);

  // Handle search focus/blur animations
  useEffect(() => {
    if (isSearchFocused) {
      // Trigger blur effect immediately when focused
      setIsBlurred(true);
    } else {
      // Remove blur effect with delay for smooth transition
      setTimeout(() => setIsBlurred(false), 300);
    }
  }, [isSearchFocused]);

  const categories = [
    { id: 'all', label: 'All', icon: Sparkles },
    { id: 'adventure', label: 'Adventure', icon: Mountain },
    { id: 'beach', label: 'Beach', icon: Waves },
    { id: 'cultural', label: 'Cultural', icon: TreePine },
  ];

  const posts = useMemo(
    () => [
      {
        id: 1,
        author: 'cacun',
        time: '2 months ago',
        content: 'We lore free trip in Varais',
        image: 'https://picsum.photos/seed/cacun-post/700/420',
        likes: 42,
        comments: 8,
        category: 'adventure',
      },
      {
        id: 2,
        author: 'traveler_jane',
        time: '1 week ago',
        content: 'Amazing sunset at Santorini! 🌅 Who wants to join next month?',
        image: 'https://picsum.photos/seed/santorini/700/420',
        likes: 128,
        comments: 24,
        category: 'beach',
      },
      {
        id: 3,
        author: 'backpacker_mike',
        time: '3 days ago',
        content: 'Hidden gems in Kyoto - temples and gardens 🏯',
        image: 'https://picsum.photos/seed/kyoto/700/420',
        likes: 89,
        comments: 15,
        category: 'cultural',
      },
      {
        id: 4,
        author: 'adventure_seeker',
        time: '5 hours ago',
        content: 'Which could sunny 🙇',
        image: null,
        likes: 23,
        comments: 4,
        category: 'adventure',
      },
    ],
    []
  );

  const filteredPosts = useMemo(() => {
    if (selectedCategory === 'all') return posts;
    return posts.filter(post => post.category === selectedCategory);
  }, [posts, selectedCategory]);

  const [filters, setFilters] = useState({ type: 'All', time: 'All Time', from: 'All time Bnd' });
  const handleFind = () => {
    const label = `${filters.type} • ${filters.time} • ${filters.from}`;
    alert(`Searching partners: ${label}`);
  };

  return (
    <div className="home-dashboard" style={{
      minHeight: '100vh',
      position: 'relative',
      zIndex: 1
    }}>
      {/* Blur Background Overlay - Lower z-index */}
      {isBlurred && (
        <div 
          className="fixed inset-0 bg-black/20 backdrop-blur-sm transition-all duration-500 ease-in-out"
          style={{
            backdropFilter: isBlurred ? 'blur(8px)' : 'blur(0px)',
            WebkitBackdropFilter: isBlurred ? 'blur(8px)' : 'blur(0px)',
            zIndex: 10
          }}
        />
      )}
      
      {/* Content Layer - Higher z-index to appear above blur */}
      <div style={{ position: 'relative', zIndex: 20 }}>
        <div className="home-search">
          <div className="relative flex items-center gap-2">
            <div className={`relative flex-1 transition-all duration-700 ease-out ${isSearchFocused ? 'flex-grow' : ''}`}>
              <Search className="home-search-icon" />
              <input
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onFocus={() => {
                  setSearchOpen(true);
                  setIsSearchFocused(true);
                }}
                onBlur={() => {
                  setTimeout(() => {
                    setSearchOpen(false);
                    setIsSearchFocused(false);
                  }, 150);
                }}
                onKeyPress={(e) => {
                  if (e.key === 'Enter') {
                    handleSearch(searchQuery);
                    setSearchOpen(false);
                  } else if (e.key === 'Tab') {
                    // Tab key pressed - exit search focus
                    e.preventDefault();
                    setSearchOpen(false);
                    setIsSearchFocused(false);
                  }
                }}
                placeholder="Search features, trips, or people..."
                className={`home-search-input w-full px-12 py-4 rounded-xl text-text placeholder-text-muted focus:outline-none transition-all duration-700 ease-out ${
                  isSearchFocused ? 'px-16 py-5' : 'px-12 py-4'
                }`}
                style={{
                  fontSize: isSearchFocused ? '16px' : '14px',
                  width: isSearchFocused ? '450px !important' : '200px !important',
                  maxWidth: '100%',
                  transition: 'all 0.7s cubic-bezier(0.68, -0.55, 0.265, 1.55)',
                  zIndex: 30
                }}
              />

              {/* Enhanced Search Results with location indicators */}
              <div className={`transition-all duration-700 ease-out ${
                searchOpen && isSearchFocused ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-3 pointer-events-none'
              }`}
                style={{ zIndex: 40 }}>
                {searchOpen && allSearchResults.length > 0 && (
                  <div className="home-search-results">
                    {allSearchResults.map((item, index) => (
                      <div
                        key={item.id}
                        className="transition-all duration-500"
                        style={{
                          animationDelay: `${index * 60}ms`,
                          animation: searchOpen ? 'bounceIn 0.4s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards' : 'bounceOut 0.3s ease-in forwards'
                        }}
                      >
                        <button
                          type="button"
                          className="home-search-result"
                          onMouseDown={(e) => e.preventDefault()}
                          onClick={() => {
                            setSearchOpen(false);
                            handleSearch(item.title);
                            
                            // Navigate based on type and location
                            if (item.type === 'feature') {
                              item.action();
                            } else if (item.type === 'chat') {
                              navigate('/messages');
                            } else if (item.type === 'option') {
                              if (item.location === 'Profile') navigate('/profile');
                              else if (item.location === 'Settings') navigate('/settings');
                              else if (item.location === 'Messages') navigate('/messages');
                              else navigate('/home');
                            }
                          }}
                        >
                          <div className="flex items-center justify-between w-full">
                            <div className="flex-1 text-left">
                              <div className="text-text font-semibold text-sm">{item.title}</div>
                              <div className="text-text-muted text-xs mt-1">{item.description}</div>
                            </div>
                            <div className="flex items-center gap-2">
                              {/* Type indicator */}
                              <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                item.type === 'feature' ? 'bg-blue-500 text-white' :
                                item.type === 'chat' ? 'bg-green-500 text-white' :
                                item.type === 'option' ? 'bg-purple-500 text-white' :
                                'bg-gray-500 text-white'
                              }`}>
                                {item.type === 'feature' ? 'Feature' :
                                 item.type === 'chat' ? 'Chat' :
                                 item.type === 'option' ? 'Option' : 'Item'}
                              </span>
                              {/* Location indicator */}
                              <span className="text-text-muted text-xs">
                                {item.location}
                              </span>
                            </div>
                          </div>
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            {/* Filter Button - Appears when search is not focused with bouncy animation */}
            <div className={`transition-all duration-700 ease-out transform-origin-right ${
              !isSearchFocused ? 'opacity-100 translate-x-0 scale-100' : 'opacity-0 -translate-x-8 scale-90 pointer-events-none'
            }`}
              style={{ zIndex: 35 }}>
              <div className="relative">
                <button
                  onClick={() => setShowFilters(!showFilters)}
                  className="flex items-center gap-2 px-3 py-2 rounded-xl glass-container border border-white/10 transition-all duration-300"
                  style={{ 
                    color: 'var(--text-muted)',
                    WebkitTapHighlightColor: 'transparent',
                    WebkitTouchCallout: 'none',
                    WebkitUserSelect: 'none',
                    userSelect: 'none',
                    outline: 'none',
                    border: 'none',
                    background: 'transparent',
                    cursor: 'pointer',
                    transform: 'none',
                    height: '48px',
                    minHeight: '48px'
                  }}
                >
                  <Filter className="w-4 h-4" />
                  <span className="text-sm">Filters</span>
                </button>

                {showFilters && (
                  <div ref={filterRef} className="absolute right-0 top-full mt-2 w-64 rounded-2xl overflow-hidden glass-container border border-white/10 transition-all duration-300"
                    style={{
                      zIndex: 50,
                      animation: 'bounceInDown 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards'
                    }}
                  >
                    <div className="p-4">
                      <h3 className="text-text font-semibold mb-3">Quick Filters</h3>
                      <div className="space-y-2">
                        {categories.map((cat, index) => {
                          const IconComp = cat.icon;
                          return (
                            <div
                              key={cat.id}
                              className="transition-all duration-500"
                              style={{
                                animationDelay: `${index * 100}ms`,
                                animation: 'bounceInLeft 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55) forwards'
                              }}
                            >
                              <button
                                onClick={() => {
                                  setSelectedCategory(cat.id);
                                  setShowFilters(false);
                                }}
                                className={`w-full flex items-center gap-3 px-3 py-2 rounded-xl transition ${
                                  selectedCategory === cat.id
                                    ? 'bg-primary/20 text-primary'
                                    : 'hover:bg-white/10 text-text'
                                }`}
                              >
                                <IconComp className="w-4 h-4" />
                                <span className="text-sm">{cat.label}</span>
                              </button>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
        
        {/* Recent Searches */}
        <div className={`transition-all duration-700 ease-out ${
          !isSearchFocused ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-2 pointer-events-none'
        }`}
          style={{ zIndex: 25 }}>
          {recentSearches.length > 0 && (
            <div className="mt-3 flex items-center gap-2 overflow-x-auto pb-2">
              <Clock className="w-4 h-4 text-text-muted flex-shrink-0" />
              {recentSearches.map((search, index) => (
                <div
                  key={index}
                  className="transition-all duration-300"
                  style={{
                    animationDelay: `${index * 30}ms`,
                    animation: !isSearchFocused ? 'bounceIn 0.3s ease-out forwards' : 'bounceOut 0.2s ease-in forwards'
                  }}
                >
                  <button
                    onClick={() => {
                      setSearchQuery(search);
                      handleSearch(search);
                    }}
                    className="px-3 py-1 bg-white/10 border border-white/20 rounded-full text-xs text-text whitespace-nowrap hover:bg-white/20 transition"
                  >
                    {search}
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="dashboard-left">
          {/* Notifications Card */}
          <CacunContainer className="glass-container" hover>
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-primary" />
                <h2 className="text-text font-semibold">Notifications</h2>
                {notifications.filter(n => !n.read).length > 0 && (
                  <span className="px-2 py-1 bg-primary text-white text-xs rounded-full">
                    {notifications.filter(n => !n.read).length}
                  </span>
                )}
              </div>
              <button 
                onClick={() => setNotifications([])}
                className="text-text-muted hover:text-text transition text-xs"
              >
                Clear all
              </button>
            </div>
            
            <div className="space-y-3 max-h-48 overflow-y-auto">
              {notifications.slice(0, 3).map(notification => (
                <div
                  key={notification.id}
                  onClick={() => markNotificationRead(notification.id)}
                  className={`p-3 rounded-xl cursor-pointer transition ${
                    notification.read ? 'bg-white/5' : 'bg-primary/10'
                  }`}
                >
                  <div className="flex items-start gap-3">
                    <div className={`w-2 h-2 rounded-full mt-2 flex-shrink-0 ${
                      notification.read ? 'bg-transparent' : 'bg-primary'
                    }`} />
                    <div className="flex-1">
                      <p className="text-text text-sm">{notification.text}</p>
                      <p className="text-text-muted text-xs mt-1">{notification.time}</p>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CacunContainer>

          {/* Posts Feed */}
          <div className="mt-6 space-y-6">
            {filteredPosts.map(post => (
              <CacunContainer key={post.id} className="glass-container" hover>
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                      <span className="text-primary font-semibold">{post.author.slice(0, 1).toUpperCase()}</span>
                    </div>
                    <div>
                      <div className="text-text font-semibold leading-5">{post.author}</div>
                      <div className="text-text-muted text-xs">{post.time}</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => handleSavePost(post.id)}
                      className={`p-2 rounded-lg transition ${
                        savedPosts.includes(post.id)
                          ? 'text-primary bg-primary/20'
                          : 'text-text-muted hover:text-text hover:bg-white/10'
                      }`}
                    >
                      <Bookmark className={`w-4 h-4 ${savedPosts.includes(post.id) ? 'fill-current' : ''}`} />
                    </button>
                    <button aria-label="More" className="text-text-muted hover:text-text transition">
                      <MoreVertical className="w-5 h-5" />
                    </button>
                  </div>
                </div>

                <div className="text-text-muted text-sm mb-3">{post.content}</div>

                {post.image && (
                  <img
                    src={post.image}
                    alt="Post"
                    className="w-full h-56 object-cover rounded-2xl mb-4"
                  />
                )}

                <div className="flex items-center gap-4 text-text-muted">
                  <button
                    aria-label="Like"
                    onClick={() => setLikedPost((v) => !v)}
                    className={`transition ${likedPost ? 'text-primary' : 'hover:text-primary'}`}
                  >
                    <Heart className={`w-5 h-5 ${likedPost ? 'fill-primary' : ''}`} />
                    <span className="text-xs ml-1">{post.likes}</span>
                  </button>
                  <button
                    aria-label="Comment"
                    onClick={() => navigate('/messages')}
                    className="hover:text-primary transition flex items-center gap-1"
                  >
                    <MessageCircle className="w-5 h-5" />
                    <span className="text-xs">{post.comments}</span>
                  </button>
                  <button
                    aria-label="Send"
                    onClick={() => alert('Sharing coming soon')}
                    className="hover:text-primary transition"
                  >
                    <Send className="w-5 h-5" />
                  </button>
                  <div className="flex-1" />
                </div>
              </CacunContainer>
            ))}
          </div>
        </div>

        <div className="dashboard-center">
          <div ref={highlightsRef} />
          <CacunContainer className="glass-container" hover>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-text font-semibold">Trip Highlights</h2>
              <div className="flex items-center gap-2">
                <button
                  aria-label="Previous"
                  onClick={() => setActiveHighlight((v) => (v - 1 + highlights.length) % highlights.length)}
                  className="dashboard-icon-btn"
                >
                  <ChevronLeft className="w-5 h-5" />
                </button>
                <button
                  aria-label="Next"
                  onClick={() => setActiveHighlight((v) => (v + 1) % highlights.length)}
                  className="dashboard-icon-btn"
                >
                  <ChevronRight className="w-5 h-5" />
                </button>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-3">
              {[0, 1, 2].map((offset) => {
                const idx = (activeHighlight + offset) % highlights.length;
                const item = highlights[idx];
                return (
                  <img
                    key={item.id}
                    src={`https://picsum.photos/seed/${item.seed}/420/420`}
                    alt="Highlight"
                    className="w-full aspect-square object-cover rounded-2xl"
                  />
                );
              })}
            </div>

            <div className="flex items-center justify-center gap-2 mt-4">
              {highlights.map((_, i) => (
                <button
                  key={i}
                  aria-label={`Go to ${i + 1}`}
                  onClick={() => setActiveHighlight(i)}
                  className={`w-2 h-2 rounded-full transition ${i === activeHighlight ? 'bg-primary' : 'bg-white/20'}`}
                />
              ))}
            </div>
          </CacunContainer>

          <div ref={reviewsRef} />
          <CacunContainer className="glass-container mt-6" hover>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-text font-semibold">Partner Reviews</h2>
              <div className="text-text-muted text-sm">All</div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              {reviews.slice(0, 2).map((review) => (
                <div key={review.id} className="rounded-2xl bg-background/30 border border-white/10 p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                        <span className="text-primary font-semibold">{review.name.slice(0, 1)}</span>
                      </div>
                      <div>
                        <div className="text-text font-semibold leading-5">{review.name}</div>
                        <div className="text-text-muted text-xs">{review.role}</div>
                      </div>
                    </div>
                    <div className="flex items-center gap-1">
                      {Array.from({ length: 5 }).map((__, i) => (
                        <Star
                          key={i}
                          className={`w-4 h-4 ${i < review.rating ? 'fill-primary text-primary' : 'text-primary/30'}`}
                        />
                      ))}
                    </div>
                  </div>

                  <p className="text-text-muted text-sm mt-3 leading-5">{review.text}</p>
                </div>
              ))}
            </div>
          </CacunContainer>
        </div>

        <div className="dashboard-right">
          <CacunContainer className="glass-container" hover>
            <div className="flex flex-col items-center text-center">
              <div className="w-20 h-20 rounded-full bg-background/40 border-2 border-primary/60 p-1">
                <img
                  src="https://picsum.photos/seed/profile/200/200"
                  alt="Profile"
                  className="w-full h-full rounded-full object-cover"
                />
              </div>
              <div className="text-text font-semibold mt-3">Jomnaheas</div>
              <div className="text-text-muted text-xs">11 months ago</div>
              <div className="mt-3 px-4 py-2 rounded-full bg-background/35 border border-white/10 text-sm text-text">
                Reputation: 132
              </div>
            </div>
          </CacunContainer>

          <div ref={findPartnerRef} />
          <CacunContainer className="glass-container mt-6" hover>
            <h2 className="text-text font-semibold mb-4">Find Travel Partner</h2>
            <div className="space-y-3">
              <select
                value={filters.type}
                onChange={(e) => setFilters((s) => ({ ...s, type: e.target.value }))}
                className="dashboard-select"
              >
                <option>All</option>
                <option>Adventure</option>
                <option>City</option>
                <option>Nature</option>
              </select>
              <select
                value={filters.time}
                onChange={(e) => setFilters((s) => ({ ...s, time: e.target.value }))}
                className="dashboard-select"
              >
                <option>All Time</option>
                <option>Last 30 Days</option>
                <option>Last 6 Months</option>
                <option>Last Year</option>
              </select>
              <select
                value={filters.from}
                onChange={(e) => setFilters((s) => ({ ...s, from: e.target.value }))}
                className="dashboard-select"
              >
                <option>All time Bnd</option>
                <option>From nearby</option>
                <option>From my city</option>
              </select>
              <button onClick={handleFind} className="dashboard-find-btn">Find</button>
            </div>
          </CacunContainer>
        </div>
      </div>
    </div>
  );
}
