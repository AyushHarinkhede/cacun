module.exports = {
  content: ["./src/**/*.{js,jsx,ts,tsx}", "./public/index.html"],
  theme: {
    extend: {
      colors: {
        primary: {
          special: '#E67E5F', // Cacun Coral
        },
        background: {
          DEFAULT: '#0F2C33', // Deep Ocean
        },
        card: {
          DEFAULT: '#1A424A', // Reef Teal
        },
        text: {
          main: '#F5F5F5',
          muted: '#8EAeb4',
        },
      },
      borderRadius: {
        'cacun': '24px 24px 24px 4px',
      },
    },
  },
  plugins: [],
};