# Cacun - MERN Stack Redesign

A complete rebuild of the Cacun webapp using the MERN stack (MongoDB, Express, React, Node.js). 

<div style="display: flex; align-items: center; justify-content: flex-start; gap: 0px;">
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&pause=3000&color=9643F7&background=00000000&center=false&vCenter=true&width=85&lines=cacun" alt="cacun" />
  
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&pause=3000&color=FF0000&background=00000000&center=false&vCenter=true&width=50&startDelay=1500&lines=is" alt="is" />
  
  <img src="https://readme-typing-svg.herokuapp.com?font=Fira+Code&pause=3000&color=23F709&background=00000000&center=false&vCenter=true&width=220&startDelay=3000&lines=Live!🌱" alt="Live" />
</div> 
 
---

## 🌍 About Cacun

Cacun is a revolutionary **nature-first marketplace** dedicated to promoting sustainable living through eco-friendly products. Our platform connects conscious consumers with vendors who share our commitment to environmental protection.

### 🎯 Our Mission

To create a world where every purchase contributes to a healthier planet by offering:
- **Plastic-free alternatives**
- **Non-toxic products**
- **Recycled materials**
- **Nature-based solutions**
- **Reusable options**

## ✨ Key Features

### 🛍️ **Product Categories**
- **Plastic Free** - Packaging and products with zero plastic use
- **Non Toxic** - Safe beauty, soap, detergents, farm items and daily essentials
- **Recycled Material** - Shoes, clothes, carry bags, pouches, boxes, furniture and more
- **Nature Products** - Leafy plates, edible spoons, coconut coir scrub, organic skincare
- **Reuse Products** - Refillable bottles, reusable shampoo packaging, cleaner capsules

### 🌱 **Campaigns & NGOs**
- Join clean-earth missions and track impact through your purchases
- Support verified organizations working on waste reduction and nature protection
- Real-time impact tracking and reporting

### 📱 **Modern User Experience**
- **Responsive Design** - Works seamlessly on all devices
- **Royal Theme** - Premium royal blue and gold color scheme
- **Smooth Animations** - Engaging scroll-triggered animations
- **Advanced Search** - Find products quickly with intelligent search
- **Social Integration** - Connect with eco-conscious community

## 🛠️ **Technology Stack**

### **Frontend**
- **React** - Modern component-based architecture
- **CSS3** - Advanced styling with custom properties
- **JavaScript ES6+** - Modern JavaScript features
- **Responsive Grid/Flexbox** - Mobile-first design approach

### **Design System**
- **Royal Blue & Gold Theme** - Premium color palette
- **Custom Animations** - Smooth transitions and micro-interactions
- **Component Library** - Reusable UI components
- **Accessibility** - WCAG compliant design

## 🚀 **Getting Started**

### **Prerequisites**
- Node.js (v14 or higher)
- npm or yarn
- Modern web browser

### **Installation**
```bash
# Clone the repository
git clone https://github.com/AyushHarinkhede/cacun.git

# Navigate to project directory
cd cacun

# Install dependencies
npm install

# Start development server
npm start
```

### **Build for Production**
```bash
# Build optimized production bundle
npm run build

# Preview production build
npm run preview
```

## 📁 **Project Structure**
=======
## Project Structure
>>>>>>> e412424 (cacun new redesign)

```
cacun/
├── server/                 # Node.js/Express backend
│   ├── config/            # Configuration files
│   ├── controllers/        # Route controllers
│   ├── models/            # MongoDB schemas
│   ├── routes/            # API routes
│   ├── middleware/        # Custom middleware
│   ├── server.js          # Entry point
│   ├── package.json
│   └── .env              # Environment variables (not in git)
│
├── client/               # React frontend
│   ├── public/           # Static files
│   ├── src/
│   │   ├── components/   # React components
│   │   ├── pages/        # Page components
│   │   ├── hooks/        # Custom hooks
│   │   ├── utils/        # Utility functions
│   │   ├── styles/       # CSS/styling
│   │   ├── App.jsx       # Main app component
│   │   └── main.jsx      # Entry point
│   ├── package.json
│   ├── vite.config.js
│   └── .env             # Environment variables (not in git)
│
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites
- Node.js (v14 or higher)
- MongoDB
- npm or yarn

### Installation

1. **Install Server Dependencies**
```bash
cd server
npm install
```

2. **Install Client Dependencies**
```bash
cd ../client
npm install
```

### Configuration

1. Create `.env` file in the `server/` directory:
```
MONGO_URI=mongodb://localhost:27017/cacun
PORT=5000
NODE_ENV=development
JWT_SECRET=your_jwt_secret_here
```

2. Create `.env` file in the `client/` directory:
```
VITE_API_URL=http://localhost:5000
```

### Running the Application

**Development Mode:**

Terminal 1 - Start the server:
```bash
cd server
npm run dev
```

Terminal 2 - Start the client:
```bash
cd client
npm run dev
```

**Production Mode:**

Build the client:
```bash
cd client
npm run build
```

Start the server with environment set to production:
```bash
cd server
NODE_ENV=production npm start
```

## Technology Stack

### Backend
- **Node.js** - Runtime environment
- **Express** - Web framework
- **MongoDB** - NoSQL database
- **Mongoose** - ODM for MongoDB
- **JWT** - Authentication

### Frontend
- **React** - UI library
- **Vite** - Build tool
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client

## Development

### Available Scripts

**Server:**
- `npm run dev` - Start server with hot reload
- `npm start` - Start production server

**Client:**
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add new feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a pull request

## License

MIT
