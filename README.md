# 🚌 Shukisha Please — Smart Commuter Stop Alerts & Matatu Games

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-brightgreen.svg)

> **"Tushukishe wapi leo?"** — Never miss your matatu stop again!

**Shukisha Please** is a native Android application designed specifically for Kenyan commuters on matatus, buses, and commuter rail. It tracks your commute in the background using high-accuracy GPS and alerts you with a loud, full-screen incoming-call style alarm before you arrive at your stop — even if your phone is on silent, locked, or running low on battery.

---

## ✨ Key Features

### 🗺️ Live GPS Tracking & Commute ETA Engine
- **Fused Location Tracking:** Real-time GPS location updates with a dynamic moving bus marker and route progress line.
- **Smart Route ETA Calculation:** Differentiates urban traffic congestion routes from upcountry highways, dynamically adjusting ETAs based on live speed and stage stop allowances.
- **GPS Watchdog:** Monitors GPS satellite health and notifies you if signal drops in tunnels or deep upcountry.

### 🚨 Bulletproof Background Alarms
- **Foreground Location Service (`TripTrackingService`):** Keeps watching your trip continuously, even if you close the app or lock your screen.
- **Exact Hardware Backup Alarms (`AlarmManager`):** Schedules hardware alarm clocks as a backup in case GPS signal is lost.
- **Full-Screen Arrival Alert:** Rings a loud incoming-call style alert (`IncomingCallAlertActivity`) that loops audio on silent mode, requiring a 1-second hold (*"Hold — I'm awake"*) to dismiss.
- **Reboot Recovery:** Automatically restores active trip tracking if your device restarts during a commute.

### 🛡️ Emergency SOS & Safety Sharing
- **Hold-to-Trigger SOS:** 2-second hold button to trigger emergency actions: direct **Call 999** dialer or live location sharing.
- **Trusted Contacts:** Select up to 3 trusted contacts directly from your phone book.
- **Night Safety Prompts:** Automatically prompts you to share your live location when commuting after dark (*"Riding after dark 🌙"*).

### 🎮 Commuter Mini-Game Hub
- **4 Local Kenya Games:** *Kenya Quiz*, *Unscramble It*, *Missing Letters*, and *This or That*.
- **Auto-Advancing Gameplay:** Seamless 1.2s auto-advance upon locking in answers.
- **Real-Time Coin Sync:** Points earned in games update your Home screen coin balance dynamically.

### 🎨 Matatu Culture Neo-Brutalist Design
- High-contrast Jetpack Compose UI with bold black borders, vibrant Kenyan colors, custom Makanga mascot illustrations, and adaptive launcher icons.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 100%
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture:** MVVM + StateFlow + Clean Architecture
- **Location Services:** Google Play Services `FusedLocationProviderClient`
- **Background Operations:** Android Foreground Service (`foregroundServiceType="location"`)
- **Hardware Alarms:** `AlarmManager` (`setAlarmClock`)
- **Persistence:** `SharedPreferences` (`CommuterPrefs`)
- **Unit Testing:** JUnit4

---

## 🚀 How to Build & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/JemimaNyaguthii/ShukishaPlease.git
   ```
2. **Open in Android Studio:**
   - Open Android Studio (Ladybug or newer).
   - Select **Open an Existing Project** and choose `ShukishaPlease`.
3. **Build & Run:**
   - Connect an Android device or launch an emulator (API 26+).
   - Press **Run** (or execute `./gradlew assembleDebug` in terminal).

---

## 👤 Author

**Jemima Nyaguthii**  
- GitHub: [@JemimaNyaguthii](https://github.com/JemimaNyaguthii)

---
*Developed with ❤️ for Kenyan Commuters.*
