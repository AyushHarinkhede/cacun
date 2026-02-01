import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Compass, PlusCircle, MessageCircle, User } from 'lucide-react';

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
    <div className="bottom-nav">
      <div className="bottom-nav-container">
        <div className="flex items-center gap-3">
          {items.map((item) => {
            const IconComp = item.icon;
            if (item.id === 'create') {
              return (
                <button
                  key={item.id}
                  onClick={() => handleNavigation(item.id)}
                  aria-label={item.label}
                  className="-mt-6 flex items-center justify-center w-14 h-14 rounded-full shadow-xl transition-all duration-300 transform hover:scale-110 bg-white"
                >
                  <IconComp className="w-7 h-7 text-primary" />
                </button>
              );
            }

            return (
              <button
                key={item.id}
                onClick={() => handleNavigation(item.id)}
                aria-label={item.label}
                className="p-2 rounded-full transition-all duration-200"
              >
                <div className="flex flex-col items-center space-y-1">
                  <IconComp className={`w-5 h-5 ${active === item.id ? 'text-white' : 'text-white/80'}`} />
                  <span className={`text-xs ${active === item.id ? 'text-white' : 'text-white/80'}`}>
                    {item.label}
                  </span>
                </div>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
