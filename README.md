# Cacun - Glassmorphic System HUD (Android)

Cacun is a premium, interactive, and hardware-aware **System Diagnostics HUD** for Android. Rebuilt entirely from scratch in Kotlin, it utilizes high-performance low-level Java API bindings to monitor system parameters while allowing active control of hardware interfaces.

The UI is built with Jetpack Compose featuring a state-of-the-art **Glassmorphic Cyber-Console** theme, optimized for both mobile phones and tablets in portrait orientation.

---

## 🚀 Key Features

* **Kernel Boot Diagnostics Sequence:** Animates an interactive command-line log console on startup typing out system mounting and sensor checks.
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

---

## 🛠️ Technology Stack
* **Language:** Kotlin (HUD View) & Java (Hardware Telemetry API bindings)
* **UI Framework:** Jetpack Compose (Material 3 with custom glassmorphism)
* **Build System:** Gradle (Kotlin DSL - Android Gradle Plugin 9.0)
* **Offboard Telemetry Agent:** Python 3 (ADB Shell connection utility)

---

## 🐍 Offboard Python Telemetry Exporter

For off-device diagnostics, you can run the offboard Python agent. Connect your phone via USB with USB Debugging enabled, and run:

```bash
python telemetry_exporter.py
```

This script interfaces with ADB to pull device info, battery health, current drain, power wattage, storage metrics, screen brightness, system stream volume, and active hardware sensors remotely.

---

## ⚙️ Compilation & Build

To compile the application and generate the debug APK, run:

```powershell
.\gradlew assembleDebug
```
The compiled APK will be available under `app/build/outputs/apk/debug/`.
