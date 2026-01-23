export default {
  content: [
    './index.html',
    './src/**/*.{js,jsx,ts,tsx}',
  ],
  theme: {
    // Use a custom palette for the whole app (avoid relying on Tailwind default colors)
    colors: {
      primary: '#E67E5F', // Cacun Coral - main actions
      background: '#0F2C33', // Deep Ocean - app background
      card: '#1A424A', // Reef Teal - cards/containers
      text: {
        DEFAULT: '#F5F5F5',
        muted: '#8EAeb4',
      },
      transparent: 'transparent',
      current: 'currentColor',
    },
    extend: {},
  },
  plugins: [],
};
