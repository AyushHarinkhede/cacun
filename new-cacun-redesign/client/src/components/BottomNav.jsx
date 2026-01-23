import React, { useState } from "react";
import { HomeIcon, SearchIcon, PlusCircleIcon, ChatIcon, UserIcon } from "@heroicons/react/solid";

const navItems = [
  { icon: <HomeIcon className="w-6 h-6" />, label: "Home" },
  { icon: <SearchIcon className="w-6 h-6" />, label: "Explore" },
  { icon: <PlusCircleIcon className="w-10 h-10" />, label: "Add Trip", central: true },
  { icon: <ChatIcon className="w-6 h-6" />, label: "Messages" },
  { icon: <UserIcon className="w-6 h-6" />, label: "Profile" },
];

export default function BottomNav() {
  const [active, setActive] = useState(0);
  return (
    <nav className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-50">
      <div className="flex items-center bg-primary-special bg-opacity-90 border border-primary-special rounded-full shadow-lg px-6 py-2 gap-6 backdrop-blur-lg" style={{ minWidth: 320 }}>
        {navItems.map((item, idx) => (
          <button
            key={item.label}
            className={`flex flex-col items-center justify-center transition-transform duration-200 ${active === idx ? "scale-110" : "scale-100"} ${item.central ? "mx-2" : "mx-1"}`}
            onClick={() => setActive(idx)}
            aria-label={item.label}
          >
            {item.icon}
          </button>
        ))}
      </div>
    </nav>
  );
}
