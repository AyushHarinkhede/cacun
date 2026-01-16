module.exports = {
  content: [
    "./Client/**/*.{js,jsx,ts,tsx}",
    "./cacun/Client/**/*.{js,jsx,ts,tsx}",
    "./index.html"
  ],
  theme: {
    extend: {
      fontFamily: {
        inter: ["Inter", "sans-serif"],
        outfit: ["Outfit", "sans-serif"],
      },
      colors: {
        obsidian: "#18181b",
        neonPink: "#ff4ecd",
        deepPurple: "#7c3aed",
        mesh1: "#2d0036",
        mesh2: "#ff4ecd",
        mesh3: "#ffb86c",
      },
      backgroundImage: {
        'mesh-gradient': 'radial-gradient(ellipse at 20% 20%, #7c3aed 0%, #ff4ecd 40%, #18181b 100%)',
        'hero-gradient': 'linear-gradient(90deg, #7c3aed 0%, #ff4ecd 50%, #ffb86c 100%)',
      },
      borderRadius: {
        xl: '2rem',
        '3xl': '3rem',
      },
      boxShadow: {
        glass: '0 8px 32px 0 rgba(31, 38, 135, 0.37)',
        glow: '0 0 16px 4px #ff4ecd',
      },
      backdropBlur: {
        glass: '16px',
      },
      letterSpacing: {
        wide: '.08em',
        wider: '.15em',
      },
    },
  },
  plugins: [require('@tailwindcss/typography')],
};
