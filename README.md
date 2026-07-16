# Cacun - Dual Platform Ecosystem

Cacun is a multi-platform ecosystem consisting of:
1. **📱 Android System HUD:** A premium, interactive, and hardware-aware system diagnostics application built in **Kotlin** and **Java** with a glassmorphic console theme.
2. **🌍 Nature-First Sustainable Marketplace:** A responsive web application built using the **MERN Stack** (MongoDB, Express, React, Node.js) with a royal blue and gold design system.

---

# 📱 Part 1: Android System HUD (Kotlin & Java)

Cacun Android HUD is an interactive, hardware-aware **System Diagnostics HUD** for Android. It interfaces directly with low-level Android frameworks to monitor system parameters while allowing active control of hardware interfaces.

## 🚀 HUD Key Features

* **Kernel Boot Diagnostics Sequence:** Animates an interactive command-line log console on startup typing out system mounting and sensor checks beneath the glowing Cacun logo.
* **Low-Power Telemetry Engine:**
  * **Lifecycle Awareness:** Sensor listeners and broadcast receivers are bound strictly during `onResume` and **fully unregistered** in `onPause` when the app is in the background or screen is locked. This ensures **0% background CPU / battery drain**.
  * **Eco-Throttling Mode:** Switch between Fast, Standard, and Eco Mode. Eco Mode decreases sensor polling intervals to 1000ms, conserving hardware power cycles.
* **Interactive Hardware Controls:**
  * **Rear Torch Toggle:** Turn on/off the device's physical camera LED.
  * **Music Stream Volume:** Real-time system audio volume slider using `AudioManager`.
  * **Brightness Override:** Slide to adjust local window brightness level dynamically from dimmed (5%) to maximum (100%) without needing system-wide write settings permissions.
  * **Haptic Pulse Test:** Fire custom tactile vibration waves (`CLICK`, `THUMP`, and `SOS WAVE` morse code pulse).
* **Deep Hardware Telemetry:**
  * **Network Interface:** Telemetry link type (WiFi/Cellular/Disconnected) and downstream bandwidth in **Mbps**.
  * **Oscilloscope Vector Plotter:** Canvas graph displaying live scrolling accelerometer movements (X, Y, Z mapped to neon colors).
  * **Battery Module:** Track battery percentage, voltage (V), health state, temperature (°C), charging plug type, current (mA), and calculated wattage (W).
  * **Memory Array Allocation:** Graphical indicators showing RAM usage and internal storage arrays.
  * **System Specs:** Manufacturer, device model, CPU core hardware, Android version, screen resolution, refresh rate, NFC antenna state, and camera megapixels (front/rear).
  * **Secure Codes:** Attempted IMEI read with security exception logging and Android ID fallback.

## 🐍 Offboard Python Telemetry Exporter

For off-device diagnostics, you can run the offboard Python agent. Connect your phone via USB with USB Debugging enabled, and run:

```bash
python telemetry_exporter.py
```

This script interfaces with ADB to pull device info, battery health, current drain, power wattage, storage metrics, screen brightness, system stream volume, and active hardware sensors remotely.

## ⚙️ Compilation & Build

To compile the application and generate the debug APK, run:

```powershell
.\gradlew assembleDebug
```
The compiled APK will be available under `app/build/outputs/apk/debug/`.

---

# 🌍 Part 2: Nature-First Sustainable Marketplace (MERN Stack)

Cacun web platform is a revolutionary **nature-first marketplace** dedicated to promoting sustainable living through eco-friendly products. Our platform connects conscious consumers with vendors who share our commitment to environmental protection.

### 🎯 Our Mission
To create a world where every purchase contributes to a healthier planet by offering plastic-free alternatives, non-toxic products, recycled materials, and reusable options.

## 🛍️ Web Features & Categories
- **Plastic Free** - Packaging and products with zero plastic use.
- **Non Toxic** - Safe beauty, soap, detergents, farm items, and daily essentials.
- **Recycled Material** - Shoes, clothes, carry bags, pouches, boxes, furniture, and more.
- **Nature Products** - Leafy plates, edible spoons, coconut coir scrub, organic skincare.
- **Reuse Products** - Refillable bottles, reusable shampoo packaging, cleaner capsules.
- **Campaigns & NGOs** - Join clean-earth missions and track impact through your purchases.

## 🛠️ Web Technology Stack

### **Backend**
- **Node.js** - Runtime environment
- **Express** - Web framework
- **MongoDB** - NoSQL database
- **Mongoose** - ODM for MongoDB
- **JWT** - Authentication

### **Frontend**
- **React** - Modern UI library
- **Vite** - Build tool
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client

## 🚀 Web Getting Started

### **Prerequisites**
- Node.js (v14 or higher)
- MongoDB
- npm or yarn

### **Installation**

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

### **Configuration**

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

### **Running the Application**

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

---

# 📁 Project Directory Structure

```
cacun/
├── app/                  # Android System HUD application (Kotlin / Java)
│   ├── src/main/         # Android source code & resources
│   │   ├── java/         # Telemetry bindings (Java) & main HUD view (Kotlin)
│   │   └── res/          # Layout resources & logo assets
│   └── build.gradle.kts  # App gradle build settings
│
├── server/                 # Node.js/Express backend (MERN stack)
│   ├── config/            # Configuration files
│   ├── controllers/        # Route controllers
│   ├── models/            # MongoDB schemas
│   ├── routes/            # API routes
│   └── server.js          # Entry point
│
├── client/               # React frontend (MERN stack)
│   ├── public/           # Static files
│   ├── src/
│   │   ├── components/   # React components
│   │   └── App.jsx       # Main app component
│   └── vite.config.js
│
├── telemetry_exporter.py # Remote Python ADB telemetry exporter
├── build.gradle.kts      # Root gradle build settings
├── settings.gradle.kts   # Root gradle project settings
├── .gitignore
└── README.md
```

## Contributing
1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add new feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit a pull request.

## License
MIT
