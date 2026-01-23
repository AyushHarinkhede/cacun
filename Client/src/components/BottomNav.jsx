import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Home, Compass, PlusCircle, MessageCircle, User } from 'lucide-react';

const Icon = ({ children, active }) => (
  <div
    className={`flex items-center justify-center w-10 h-10 rounded-full transition-transform duration-200 transform ${
      active ? 'scale-110' : 'scale-100'
    }`}
  >
    {children}
  </div>
);

export default function BottomNav() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const getActiveFromPath = () => {
    if (location.pathname === '/') return 'home';
    if (location.pathname === '/explore') return 'explore';
    if (location.pathname === '/messages') return 'messages';
    if (location.pathname === '/profile') return 'profile';
    return 'home';
  };
  
  const [active, setActive] = useState(getActiveFromPath());

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
        console.log('Create post clicked');
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
    <motion.div 
      initial={{ y: 100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ type: "spring", stiffness: 260, damping: 20 }}
      className="fixed left-1/2 transform -translate-x-1/2 bottom-6 z-50"
    >
      <div
        className="relative flex items-center px-6 py-3 shadow-2xl"
        style={{
          backgroundColor: '#E67E5F',
          backdropFilter: 'blur(10px)',
          borderRadius: '9999px',
        }}
      >
        <div className="flex items-center gap-3">
          {items.map((item) => {
            const IconComp = item.icon;
            if (item.id === 'create') {
              return (
                <motion.button
                  key={item.id}
                  onClick={() => handleNavigation(item.id)}
                  aria-label={item.label}
                  className="-mt-6 flex items-center justify-center w-14 h-14 rounded-full shadow-xl transition-all duration-300 transform hover:scale-110 bg-white"
                  whileHover={{ scale: 1.1 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <IconComp className="w-7 h-7 text-primary" />
                </motion.button>
              );
            }

            return (
              <motion.button
                key={item.id}
                onClick={() => handleNavigation(item.id)}
                aria-label={item.label}
                className="p-2 rounded-full transition-all duration-200"
                whileHover={{ scale: 1.1 }}
                whileTap={{ scale: 0.95 }}
              >
                <IconComp className={`w-5 h-5 ${active === item.id ? 'text-white' : 'text-white/80'}`} />
              </motion.button>
            );
          })}
        </div>
      </div>
    </motion.div>
  );
}
