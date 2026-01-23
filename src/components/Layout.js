import React from "react";
import { Home, Users, Star, Plus } from "lucide-react";
import { motion } from "framer-motion";
import cacunLogo from "../assets/cacun.png"; // Ensure this path is correct

const navItems = [
  { icon: <Home size={24} />, label: "Feed" },
  { icon: <Users size={24} />, label: "Partners" },
  { icon: <Star size={24} />, label: "Reviews" },
];

export default function Layout() {
  const [active, setActive] = React.useState(0);

  return (
    <div className="relative min-h-screen flex flex-col">
      {/* Logo at top center (Feed page) */}
      <div className="absolute top-4 left-1/2 -translate-x-1/2 z-20">
        <img src={cacunLogo} alt="Cacun Logo" className="h-10" />
      </div>

      {/* Main content */}
      <div className="flex-1 flex flex-col items-center justify-center pt-24 pb-32">
        {/* Example "Cacun Container" Card */}
        <div
          className="bg-cacun-card text-cacun-text p-6 w-80 shadow-lg"
          style={{ borderRadius: "24px 24px 24px 8px" }}
        >
          <h2 className="text-xl font-bold mb-2">Trip to Bali</h2>
          <p className="mb-4">Date: 12 Aug 2024 | Budget: $800</p>
          <button className="bg-cacun-accent text-cacun-text px-4 py-2 rounded-full font-semibold shadow hover:scale-105 transition">
            Request to Join
          </button>
        </div>
      </div>

      {/* Floating Bottom Navbar */}
      <motion.nav
        initial={{ y: 100, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ type: "spring", stiffness: 120 }}
        className="fixed bottom-6 left-1/2 -translate-x-1/2 z-30"
      >
        <div className="flex items-center bg-cacun-accent text-cacun-text px-6 py-3 rounded-full shadow-cacun-nav gap-8">
          {navItems.map((item, idx) => (
            <button
              key={item.label}
              onClick={() => setActive(idx)}
              className={`flex flex-col items-center transition ${
                active === idx ? "scale-110" : "opacity-70"
              }`}
            >
              {item.icon}
              <span className="text-xs mt-1">{item.label}</span>
            </button>
          ))}
          {/* Central Floating Button with Logo */}
          <motion.button
            whileTap={{ scale: 0.9 }}
            className="bg-cacun-bg rounded-full p-2 -mt-8 shadow-lg border-4 border-cacun-accent flex items-center justify-center"
            style={{ boxShadow: "0 2px 12px rgba(230,126,95,0.18)" }}
          >
            <img src={cacunLogo} alt="Cacun" className="h-8 w-8" />
          </motion.button>
        </div>
      </motion.nav>
    </div>
  );
}
