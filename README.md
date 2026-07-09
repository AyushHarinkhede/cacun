# Cacun - Offline Music Player (Android)

Cacun is a premium, hardware-aware **Offline Music Player** for Android. Designed with a gorgeous **Material You / Pixel UI** styling, Cacun blends dynamic color palettes with signature glassmorphic layout elements. 

---

## 🎵 What Cacun Does

Cacun turns your Android device into a high-fidelity local music studio. It scans your local storage for audio files, filters out system noises, and provides absolute control over your audio outputs. Key functions include:

* **High-Res Lossless Audio Support:** Playback engine optimized for high-resolution formats including **FLAC** and standard **MP3** files.
* **8D Spatial Audio Engine:** A dynamic sound view module that pans audio in a circular motion between your left and right ears, creating an immersive 3D surround sound experience on headphones.
* **Built-in Equalizer Studio:** Features a fully adjustable **5-band frequency equalizer** (60Hz, 230Hz, 910Hz, 4kHz, 14kHz) with support for native presets (Rock, Pop, Jazz, Classical) and custom curves.
* **Advanced Sound Effects:** Direct control over system-level **Bass Boost** and **3D Virtualizer (Surround Sound)** sliders.
* **Hardware Refresh Rate Optimization (120Hz / 1Hz):** 
  * Automatically requests **120Hz refresh rates** on active screens (during scrolling or animations) for buttery-smooth rendering.
  * Dynamically halts background update cycles when the app is paused or idle. This allows modern LTPO hardware screens to throttle down to their minimum rate (**1Hz**), preserving battery life.
* **Expandable Lyrics Cache:** Displays a scrollable, glassmorphic lyrics slider overlaying the Now Playing card. Lyrics can be edited and cached locally in an SQLite database.
* **Smarter Local Media Scanning:** Automatically queries the device's `MediaStore` and filters out audio clips shorter than 5 seconds (preventing notification tones from populating your library).
* **Background Foreground Service:** Playback continues in the background with lock screen metadata routing (`MediaSessionCompat`) and notifications. Playback auto-pauses when headphones or Bluetooth connections are unplugged.

---

## ✨ Design Aesthetic
* **Material You Pastel Colors:** Harmonious palettes that reflect Google's modern Pixel design values.
* **Glassmorphic Accents:** White border framing with translucent shapes overlaying gradient backplates.
* **Interactive Micro-Animations:** Slow rotation (25s per rotation) and scaling animations on album art that respond dynamically to play/pause actions.

---

## 🛠️ Technology Stack
* **Language:** Java (Android SDK)
* **Build System:** Gradle (Kotlin DSL - `build.gradle.kts`)
* **Database:** SQLite (`DatabaseHelper.java` for Playlists and Lyrics caching)
* **Background Playback:** Foreground Service with `MediaSessionCompat` and system `AudioManager` focus controls.
* **Audio FX:** `android.media.audiofx.Equalizer`, `android.media.audiofx.BassBoost`, and `android.media.audiofx.Virtualizer`.

---

## 🚀 Getting Started

### Prerequisites
* Android Studio (Koala/Ladybug or higher recommended)
* JDK 17
* Gradle 8.4+

### Importing the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/AyushHarinkhede/cacun.git
   ```
2. Open Android Studio and select **Open**.
3. Select the root folder `cacun`. Android Studio will automatically recognize the Gradle files and synchronize the project.

### Compiling via CLI
To build the debug APK directly from your terminal:
```bash
# Clean and assemble debug APK
.\gradlew.bat clean assembleDebug
```
The compiled APK will be output to:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📁 Project Structure

```
cacun/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/cacun/
│   │   │   │   ├── MainActivity.java        # Core UI controller and Media Scanner
│   │   │   │   ├── models/                  # Track, Album, Artist structures
│   │   │   │   ├── adapters/                # Recycler view adapters for lists & grids
│   │   │   │   ├── service/                 # Foreground Media Playback Service
│   │   │   │   ├── database/                # SQLite helper for playlists & lyrics
│   │   │   │   └── ui/                      # Tracks, Albums, Artists, Playlists, Player, EQ
│   │   │   ├── res/
│   │   │   │   ├── drawable/                # Glassmorphic backdrops, shapes, and logo
│   │   │   │   ├── layout/                  # Portrait layouts (Player, EQ, Main)
│   │   │   │   └── values/                  # Material You color maps
│   │   │   └── AndroidManifest.xml          # Background service & Storage permissions
│   └── build.gradle.kts                     # Module build settings
├── settings.gradle.kts                      # Root Gradle settings
├── build.gradle.kts                         # Project level dependencies
├── gradlew.bat                              # Windows Gradle CLI tool
└── cacun-music-player.apk                   # Compiled release preview APK
```
