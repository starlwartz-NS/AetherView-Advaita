# AetherView Advaita Ground Station Mission Control

**Team ADVAITA** | **SEVA FIRST Innovation Challenge 2026**  
Autonomous CanSat Descent, Telemetry Acquisition, and Safe Recovery.

---

## 🛰️ Standalone Desktop Web Ground Station (Primary Application)

AetherView Advaita is a **100vw × 100vh Desktop Ground Station Web Application** built with **React + TypeScript + Tailwind CSS** and optimized for Windows laptop resolutions (`1920×1080`, `1600×900`, and `1366×768`).

It is completely independent of Android Studio, Android emulators, or mobile devices, and runs directly in **Google Chrome** or **Microsoft Edge**.

---

## 🚀 How to Run Locally on Windows

### Method 1: React + Vite Development Server (Recommended)
Requires [Node.js](https://nodejs.org/) (v18+):

1. Open PowerShell or Command Prompt in the project folder.
2. Install dependencies:
   ```cmd
   npm install
   ```
3. Start the Vite development server:
   ```cmd
   npm run dev
   ```
4. Open the displayed URL in Google Chrome or Microsoft Edge:
   ```
   http://localhost:5173
   ```
5. Press **F11** for true full-screen Mission Control workstation mode.

To build and preview the optimized production bundle:
```cmd
npm run build
npm run preview
```

---

### Method 2: Python HTTP Server (Zero-Build Static Web Mode)
Requires Python 3 (built into Windows or installable via `python.org`):

1. Open PowerShell or Command Prompt in the project root folder.
2. Start the HTTP server:
   ```cmd
   python -m http.server 8080 --directory web
   ```
3. Open Google Chrome or Microsoft Edge:
   ```
   http://localhost:8080
   ```

---

### Method 3: Direct File Launch (No Servers Needed)
- Simply double-click `web/index.html` in Windows File Explorer to open directly in Google Chrome or Microsoft Edge.

---

## 🛠️ Ground Station Modules & Features

1. **Mission Overview:**
   - **5 Large KPI Cards:** Altitude AGL, Ambient Temperature, Barometric Pressure, Air Quality Index (AQI), Battery 2S Voltage.
   - **Central Descent Ladder:** High-resolution vertical ladder with 500m apogee position marker and descent velocity gauge.
   - **Atmospheric Conditions:** Air density ($\rho$), temperature lapse rate (-6.5 °C/km), relative humidity, and pressure altitude.
   - **Subsystem Health:** ESP32 240MHz MCU, SX1278 433MHz LoRa transceiver, NEO-6M GPS 3D fix, and battery status.
   - **Live Responsive Canvas Charts:** Altitude profile, temperature/pressure profile, and AQI/humidity.
   - **Recent Events Timeline, LoRa RF Link Statistics, and Live Packet Stream.**

2. **Live Telemetry:**
   - 6 synchronized waveform channels (Altitude, Velocity, Temperature, Pressure, AQI, RSSI).
   - Time horizon selector: `30s`, `60s`, `180s`, `ALL`.

3. **Flight Analytics:**
   - Flight summary metrics: Apogee achieved, max descent velocity, average descent rate under parachute, total mission duration.
   - Descent trajectory profile curve and atmospheric sounding ISA correlation.

4. **Live Tracking:**
   - **Tactical Radar Scope:** Rotating sweep beam, range rings (50m, 150m, 300m, 500m), probe blip and trail.
   - **GNSS Coordinates:** Latitude, Longitude, Altitude MSL, HDOP accuracy, ground distance, and recovery bearing.

5. **Communication:**
   - SX1278 link budget: 433.000 MHz, SF7, BW 125 kHz, CR 4/5.
   - RSSI power gauge (-40 to -110 dBm), SNR ratio meter (+9.8 dB), and PRR success rate.
   - Raw packet hex telegram (28 bytes) & decoded telemetry fields with CRC16 verification.

6. **Mission Logs & Data Export:**
   - Searchable and phase-filtered telemetry table with auto-scrolling data feed.
   - **RFC 4180 CSV Export:** Instant download of `AetherView_Advaita_Telemetry_<timestamp>.csv` containing all timestamped sensor records.

7. **System Settings:**
   - Gateway configuration for USB Serial (`COM3`) / WebSocket (`ws://localhost:8080/telemetry`).
   - Baud rate selection (115200 bps) and mission apogee target adjustments.
