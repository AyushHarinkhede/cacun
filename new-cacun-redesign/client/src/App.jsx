import React from "react";
import BottomNav from "./components/BottomNav";
import HomeFeed from "./components/HomeFeed";
import logo from "../public/cacun.png";

export default function App() {
  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* Floating Logo */}
      <div className="fixed top-4 left-4 z-50">
        <img src={logo} alt="Cacun Logo" className="w-16 h-16" />
      </div>
      {/* Home Feed */}
      <main className="flex-1 flex flex-col items-center justify-center pt-24 pb-24">
        <HomeFeed />
      </main>
      {/* Bottom Navigation */}
      <BottomNav />
    </div>
  );
}
