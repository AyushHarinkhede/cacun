<div align="center">
  <img src="./cacun.png" width="280" alt="Cacun Logo" />
  <br />
  <code><b>CACUN SYSTEM ECOSYSTEM | v2.5</b></code>
</div>

---

### 💻 `SYSTEM DIAGNOSTICS HUD` (Kotlin / Java)

```
[APP PROFILE] : Android System HUD
[STACK]       : Jetpack Compose (Kotlin) + low-level SDK Bindings (Java)
[DESIGN]      : Material U theme (No Emojis)
```

#### 🛡️ `Key Modules`
- **Dashboard:**
  - Real-time CPU clocks / throttling telemetry (`/sys/devices/system/cpu/`)
  - Live display refresh rate (60Hz, 90Hz, 120Hz, 144Hz, 240Hz+)
  - System hardware specifications (Modem, Camera, RAM, NFC)
  - Hardware Interactive Diagnostics: Multi-touch screen test & speaker frequency sweep
  - Automation Triggers UI: Macro profiles (Gaming, Eco, Performance, Battery Saver modes)
- **Spectrum:**
  - Sensor Vector Plotter with oscilloscope visualization
  - Network speed diagnostics with live traffic graphs
  - Bluetooth diagnostics with device list and wave analyzer
  - Thermal Matrix Core: Battery & CPU temperature monitoring with glowing gauges
  - Battery Infusion Module: Power calibration, charger brick rating, cable protocol detection
- **Storage:**
  - Real-time MediaStore cursor queries for Images, Videos, Audio, Documents, Downloads, and APKs
  - Neon segmented vertical progress bar for storage visualization
  - RAM Matrix Optimizer: Background process cleaner with memory purge functionality
  - Volatile Storage Sectors: Live RAM and storage usage monitoring
- **Security:**
  - Dynamic heuristic APK permission scanner & package uninstall interface
  - Screen Time tracker via UsageStatsManager
  - App Permission Analyzer: Dangerous permissions scanner (Camera, Mic, Location)
  - Network Firewall Monitor: Background data consumption tracking
- **Overlay HUD:**
  - Floating, draggable transparent widget overlaying other apps to monitor live sensor status

---

### 🌍 `SUSTAINABLE MARKETPLACE` (MERN Stack)

```
[WEB PROFILE] : Nature-First E-Commerce
[STACK]       : React + Vite + Node.js + Express + MongoDB
[DESIGN]      : Royal Blue & Gold Palette
```

#### 🛍️ `Featured Categories`
- `Plastic-Free` - Packaging & items containing zero plastics
- `Non-Toxic` - Organic health, body wash, agriculture, and beauty items
- `Recycled` - Clothes, bags, pouches, and furniture built from recycled waste
- `Reusables` - Refillable bottles and household cleaning capsules

---

### 📁 `DIRECTORY MATRIX`
```
cacun/
├── app/                  # Android System HUD application (Kotlin / Java)
├── server/               # Node.js/Express backend (MERN stack)
├── client/               # React frontend (MERN stack)
├── telemetry_exporter.py # Remote Python ADB telemetry exporter
└── cacun.png             # Cacun logo asset
```

### 🚀 `COMMANDS & SCRIPTS`

```bash
# Compile and build debug APK:
.\gradlew assembleDebug

# Run remote Python telemetry exporter:
python telemetry_exporter.py

# Launch web server:
cd server && npm install && npm run dev

# Launch web client:
cd client && npm install && npm run dev
```

---
<div align="center">
  <code>DEVELOPER : AYUSH HARINKHEDE | CONTACT : ayushharinkhere2005@gmail.com</code>
</div>
