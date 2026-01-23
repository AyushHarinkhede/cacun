import React from "react";
import './BottomNav.css';
import logo from "../assets/cacun.png";

export default function BottomNav() {
  return (
    <nav className="bottom-nav">
      <button className="nav-btn active">
        <span className="icon">🏠</span>
        <span className="label">Home</span>
      </button>
      <button className="nav-btn">
        <span className="icon">🧭</span>
        <span className="label">Explore Trips</span>
      </button>
      <button className="nav-btn main-btn">
        <img src={logo} alt="Cacun" className="main-logo" />
      </button>
      <button className="nav-btn">
        <span className="icon">✉️</span>
        <span className="label">Messages</span>
      </button>
      <button className="nav-btn">
        <span className="icon">👤</span>
        <span className="label">Profile</span>
      </button>
    </nav>
  );
}
