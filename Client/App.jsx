// src/App.jsx
import React from 'react';
import './App.css'; // CSS file import kar rahe hain
import cacunLogo from './cacun.png'; // Image import kar rahe hain

function App() {
  return (
    <div className="app-container">
      {/* Navigation Bar */}
      <nav className="navbar">
        <div className="logo">Cacun App</div>
        <ul className="nav-links">
          <li>Home</li>
          <li>About</li>
          <li>Contact</li>
        </ul>
      </nav>

      {/* Main Content / Hero Section */}
      <header className="hero-section">
        <div className="image-container">
          <img src={cacunLogo} alt="Cacun Logo" className="cacun-img" />
        </div>
        
        <h1>Welcome to Cacun</h1>
        <p>
          Yeh aapki simple web application hai. Yahan aap apna React frontend develop kar sakte hain.
        </p>
        
        <button className="primary-btn" onClick={() => alert("Button Clicked!")}>
          Get Started
        </button>
      </header>

      {/* Footer */}
      <footer className="footer">
        <p>&copy; 2024 Cacun Web Application. All rights reserved.</p>
      </footer>
    </div>
  );
}

export default App;
