/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./Client/**/*.{js,jsx,ts,tsx}",
    "./cacun/Client/**/*.{js,jsx,ts,tsx}",
    "./index.html"
  ],
  theme: {
    extend: {
      fontFamily: {
        grotesk: ["Space Grotesk", "sans-serif"],
        inter: ["Inter", "sans-serif"],
      },
      colors: {
        obsidian: "#050505",
        darkGray: "#0F0F0F",
        surface: "#111111",
        border: "#222222",
        neonPink: "#FF007F",
        electricPurple: "#7000FF",
        white: "#FFFFFF",
        'cacun-accent': '#E67E5F',
        'cacun-bg': '#0F2C33',
        'cacun-card': '#1A424A',
        'cacun-text': '#F5F5F5',
      },
      backgroundImage: {
        'accent-glow': 'radial-gradient(circle, rgba(255,0,127,0.2) 0%, rgba(112,0,255,0.1) 70%, transparent 100%)',
      },
      borderRadius: {
        sm: '8px',
        md: '12px',
        lg: '16px',
        'cacun': '24px 24px 24px 8px', // Custom "a" shape
      },
      boxShadow: {
        'neon-pink': '0 0 32px 0 rgba(255, 0, 127, 0.4)',
        'neon-purple': '0 0 32px 0 rgba(112, 0, 255, 0.4)',
        'accent-glow': '0 0 32px 0 rgba(255, 0, 127, 0.3), 0 0 2px 1px rgba(112, 0, 255, 0.6)',
        'cacun-nav': '0 4px 24px 0 rgba(230, 126, 95, 0.18)',
      },
      spacing: {
        128: '32rem',
        144: '36rem',
      },
      letterSpacing: {
        tight: '-0.05em',
        tighter: '-0.03em',
      },
    },
  },
  plugins: [],
};

