import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';

// Custom SVG Icon components
const Home = ({ className = "" }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
    <polyline points="9,22 9,12 15,12 15,22"/>
  </svg>
);

const Compass = ({ className = "" }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="12" r="10"/>
    <polygon points="16.24,7.76 14.12,14.12 7.76,16.24 9.88,9.88 16.24,7.76"/>
  </svg>
);

const PlusCircle = ({ className = "" }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <circle cx="12" cy="12" r="10"/>
    <line x1="12" y1="8" x2="12" y2="16"/>
    <line x1="8" y1="12" x2="16" y2="12"/>
  </svg>
);

const MessageCircle = ({ className = "" }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/>
  </svg>
);

const User = ({ className = "" }) => (
  <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
    <circle cx="12" cy="7" r="4"/>
  </svg>
);

export default function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();
  const [hoveredItem, setHoveredItem] = useState(null);
  
  const getActiveFromPath = () => {
    if (location.pathname === '/') return 'home';
    if (location.pathname === '/explore') return 'explore';
    if (location.pathname === '/create') return 'create';
    if (location.pathname === '/messages') return 'messages';
    if (location.pathname === '/profile') return 'profile';
    return 'home';
  };
   
  const [active, setActive] = useState(getActiveFromPath());

  useEffect(() => {
    setActive(getActiveFromPath());
  }, [location.pathname]);

  const handleNavigation = (id) => {
    setActive(id);
    switch(id) {
      case 'home':
        navigate('/');
        break;
      case 'explore':
        navigate('/explore');
        break;
      case 'create':
        navigate('/create');
        break;
      case 'messages':
        navigate('/messages');
        break;
      case 'profile':
        navigate('/profile');
        break;
    }
  };

  const items = [
    { id: 'home', label: 'Home', shortLabel: 'Home', icon: Home },
    { id: 'explore', label: 'Explore Trips', shortLabel: 'Explore', icon: Compass },
    { id: 'create', label: 'Create Post', shortLabel: 'Create', icon: PlusCircle },
    { id: 'messages', label: 'Messages', shortLabel: 'Chat', icon: MessageCircle },
    { id: 'profile', label: 'Profile', shortLabel: 'Profile', icon: User },
  ];

  return (
    <div className="bottom-nav">
      <div className="bottom-nav-container">
        <div className="bottom-nav-items">
          {items.map((item) => {
            const IconComp = item.icon;
            if (item.id === 'create') {
              return (
                <motion.button
                  key={item.id}
                  onClick={() => handleNavigation(item.id)}
                  aria-label={item.label}
                  whileTap={{ scale: 0.88 }}
                  whileHover={{ scale: 1.05, y: -2 }}
                  onHoverStart={() => setHoveredItem(item.id)}
                  onHoverEnd={() => setHoveredItem(null)}
                  transition={{ type: "spring", stiffness: 400, damping: 17 }}
                  className={`bottom-nav-create ${active === 'create' ? 'is-active' : ''}`}
                >
                  <motion.div
                    animate={{ 
                      scale: active === 'create' ? 1.1 : hoveredItem === 'create' ? 1.05 : 1,
                      rotate: active === 'create' ? [0, 360] : 0
                    }}
                    whileHover={{ rotate: [0, 360] }}
                    transition={{ 
                      rotate: { duration: 0.6, ease: "easeInOut", repeat: active === 'create' ? Infinity : 0 },
                      scale: { duration: 0.2 }
                    }}
                  >
                    <PlusCircle className="w-5 h-5 text-primary" />
                  </motion.div>
                  
                  {/* Active indicator with side label */}
                  <AnimatePresence>
                    {active === 'create' && (
                      <motion.div
                        initial={{ scale: 0, opacity: 0, x: 10 }}
                        animate={{ scale: 1, opacity: 1, x: 0 }}
                        exit={{ scale: 0, opacity: 0, x: 10 }}
                        transition={{ type: "spring", stiffness: 600, damping: 25 }}
                        className="absolute -right-12 top-1/2 transform -translate-y-1/2 bg-orange-500 text-white text-[8px] px-1 py-0.5 rounded-md font-bold shadow-md"
                      >
                        <motion.span
                          animate={{ opacity: [1, 0.5, 1] }}
                          transition={{ duration: 2, repeat: Infinity }}
                        >
                          {item.shortLabel}
                        </motion.span>
                      </motion.div>
                    )}
                  </AnimatePresence>
                </motion.button>
              );
            }

            return (
              <motion.button
                key={item.id}
                onClick={() => handleNavigation(item.id)}
                aria-label={item.label}
                whileTap={{ scale: 0.92 }}
                whileHover={{ scale: 1.05, y: -1 }}
                onHoverStart={() => setHoveredItem(item.id)}
                onHoverEnd={() => setHoveredItem(null)}
                transition={{ type: "spring", stiffness: 400, damping: 17 }}
                className={`bottom-nav-item ${active === item.id ? 'is-active' : ''}`}
              >
                <motion.div
                  animate={{ 
                    scale: active === item.id ? 1.15 : hoveredItem === item.id ? 1.05 : 1,
                    rotate: active === item.id ? [0, 10, -10, 0] : hoveredItem === item.id ? [0, 5, -5, 0] : 0
                  }}
                  whileHover={{ rotate: [0, 15, -15, 0] }}
                  transition={{ 
                    scale: { duration: 0.2 },
                    rotate: { duration: 0.4, ease: "easeInOut" }
                  }}
                >
                  {item.id === 'home' && <Home className="w-4 h-4" />}
                  {item.id === 'explore' && <Compass className="w-4 h-4" />}
                  {item.id === 'messages' && <MessageCircle className="w-4 h-4" />}
                  {item.id === 'profile' && <User className="w-4 h-4" />}
                </motion.div>
                
                {/* Active indicator with side label */}
                <AnimatePresence>
                  {active === item.id && (
                    <motion.div
                      initial={{ scale: 0, opacity: 0, x: 10 }}
                      animate={{ scale: 1, opacity: 1, x: 0 }}
                      exit={{ scale: 0, opacity: 0, x: 10 }}
                      transition={{ type: "spring", stiffness: 600, damping: 25 }}
                      className="absolute -right-12 top-1/2 transform -translate-y-1/2 bg-orange-500 text-white text-[8px] px-1 py-0.5 rounded-md font-bold shadow-md"
                    >
                      <motion.span
                        animate={{ opacity: [1, 0.5, 1] }}
                        transition={{ duration: 2, repeat: Infinity }}
                      >
                        {item.shortLabel}
                      </motion.span>
                    </motion.div>
                  )}
                </AnimatePresence>
                
                {/* Active indicator dot */}
                <AnimatePresence>
                  {active === item.id && (
                    <motion.div
                      initial={{ scale: 0, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0, opacity: 0 }}
                      transition={{ type: "spring", stiffness: 500, damping: 30 }}
                      className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-1.5 h-1.5 bg-primary rounded-full shadow-lg"
                    />
                  )}
                </AnimatePresence>
              </motion.button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
