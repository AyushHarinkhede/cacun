import { useState } from 'react';

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
  const [active, setActive] = useState('home');

  const items = [
    { id: 'home', label: 'Home', icon: HomeIcon },
    { id: 'search', label: 'Search', icon: SearchIcon },
    { id: 'add', label: 'Add', icon: PlusIcon },
    { id: 'messages', label: 'Messages', icon: MessageIcon },
    { id: 'profile', label: 'Profile', icon: ProfileIcon },
  ];

  return (
    <div className="fixed left-1/2 transform -translate-x-1/2 bottom-6 z-50">
      <div
        className="relative flex items-center px-4 py-2 rounded-full shadow-2xl"
        style={{
          backgroundColor: 'rgba(230,126,95,0.95)',
          backdropFilter: 'blur(6px)',
          borderRadius: '9999px',
        }}
      >
        <div className="flex items-center gap-4">
          {items.map((it) => {
            const IconComp = it.icon;
            if (it.id === 'add') {
              // central big button
              return (
                <button
                  key={it.id}
                  onClick={() => setActive(it.id)}
                  aria-label={it.label}
                  className="-mt-8 flex items-center justify-center w-16 h-16 rounded-full shadow-xl transition-transform duration-200 transform bg-white"
                >
                  <Icon active={active === it.id}>
                    <IconComp className="w-6 h-6 text-primary" />
                  </Icon>
                </button>
              );
            }

            return (
              <button
                key={it.id}
                onClick={() => setActive(it.id)}
                aria-label={it.label}
                className="p-1"
              >
                <Icon active={active === it.id}>
                  <IconComp className={`w-5 h-5 ${active === it.id ? 'text-white' : 'text-white/90'}`} />
                </Icon>
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}

function HomeIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
      <path d="M3 10.5L12 4l9 6.5" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M5 10.5v7a1 1 0 001 1h3v-5h6v5h3a1 1 0 001-1v-7" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function SearchIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
      <circle cx="11" cy="11" r="6" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <path d="M21 21l-4.35-4.35" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function PlusIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
      <path d="M12 5v14M5 12h14" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function MessageIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
      <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function ProfileIcon(props) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" {...props}>
      <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
      <circle cx="12" cy="7" r="4" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}
