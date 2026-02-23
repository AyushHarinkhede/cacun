import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Compass, PlusCircle, MessageCircle, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();
  
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
    { id: 'home', label: 'Home', icon: Home },
    { id: 'explore', label: 'Explore Trips', icon: Compass },
    { id: 'create', label: 'Create Post', icon: PlusCircle },
    { id: 'messages', label: 'Messages', icon: MessageCircle },
    { id: 'profile', label: 'Profile', icon: User },
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
                  transition={{ type: "spring", stiffness: 400, damping: 17 }}
                  className={`bottom-nav-create ${active === 'create' ? 'is-active' : ''}`}
                >
                  <motion.div
                    animate={{ 
                      rotate: active === 'create' ? 360 : 0,
                      scale: active === 'create' ? 1.1 : 1
                    }}
                    transition={{ duration: 0.3, ease: "easeInOut" }}
                  >
                    <IconComp className="w-7 h-7 text-primary" />
                  </motion.div>
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
                transition={{ type: "spring", stiffness: 400, damping: 17 }}
                className={`bottom-nav-item ${active === item.id ? 'is-active' : ''}`}
              >
                <div className="flex flex-col items-center space-y-1">
                  <motion.div
                    animate={{ 
                      scale: active === item.id ? 1.1 : 1,
                      rotate: active === item.id ? [0, 10, -10, 0] : 0
                    }}
                    transition={{ 
                      scale: { duration: 0.2 },
                      rotate: { duration: 0.4, ease: "easeInOut" }
                    }}
                  >
                    <IconComp className="w-5 h-5" />
                  </motion.div>
                  <motion.span 
                    className="text-xs"
                    animate={{ 
                      fontWeight: active === item.id ? 600 : 400,
                      scale: active === item.id ? 1.05 : 1
                    }}
                    transition={{ duration: 0.2 }}
                  >
                    {item.label}
                  </motion.span>
                </div>
                
                {/* Active indicator */}
                <AnimatePresence>
                  {active === item.id && (
                    <motion.div
                      initial={{ scale: 0, opacity: 0 }}
                      animate={{ scale: 1, opacity: 1 }}
                      exit={{ scale: 0, opacity: 0 }}
                      transition={{ type: "spring", stiffness: 500, damping: 30 }}
                      className="absolute -top-1 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-primary rounded-full"
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
