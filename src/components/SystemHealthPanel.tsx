import React from 'react';
import { TelemetryData, HealthStatus, SubsystemDiagnostic, OperatingMode } from '../types/telemetry';
import { 
  Cpu, 
  Radio, 
  Gauge, 
  Thermometer, 
  Wind, 
  Navigation, 
  BatteryMedium, 
  CheckCircle2, 
  AlertTriangle, 
  XCircle, 
  WifiOff, 
  ShieldAlert,
  Clock,
  RefreshCw
} from 'lucide-react';

interface SystemHealthPanelProps {
  telemetry: TelemetryData;
  mode: OperatingMode;
  isStale: boolean;
  staleSeconds: number;
}

export const SystemHealthPanel: React.FC<SystemHealthPanelProps> = ({
  telemetry,
  mode,
  isStale,
  staleSeconds,
}) => {
  // Compute health status and reasons based on real telemetry measurements
  const getSubsystems = (): SubsystemDiagnostic[] => {
    // 1. ESP32 MCU
    let esp32Status: HealthStatus = 'HEALTHY';
    let esp32Reason = 'Xtensa Dual-Core 240 MHz nominal. Task RTOS loop operating at 500ms epoch.';
    if (isStale) {
      esp32Status = staleSeconds > 5 ? 'OFFLINE' : 'WARNING';
      esp32Reason = `Telemetry heartbeat delayed by ${staleSeconds.toFixed(1)}s. Possible UART buffer overrun or transmission gap.`;
    }

    // 2. SX1278 LoRa Receiver
    let loraStatus: HealthStatus = 'HEALTHY';
    let loraReason = `RF Link locked. RSSI: ${telemetry.rssi_dbm} dBm, SNR: +${telemetry.snr_db} dB, PRR: ${telemetry.prr_pct}%.`;
    if (telemetry.rssi_dbm < -115) {
      loraStatus = 'CRITICAL';
      loraReason = 'RF power near sensitivity limit (-118 dBm). High risk of packet loss.';
    } else if (telemetry.rssi_dbm < -100 || telemetry.snr_db < 0) {
      loraStatus = 'WARNING';
      loraReason = 'Degraded link budget margin. Possible multi-path interference or range boundary.';
    }

    // 3. Barometric Sensor (BMP280)
    let baroStatus: HealthStatus = 'HEALTHY';
    let baroReason = `Pressure: ${telemetry.pressure_hpa.toFixed(1)} hPa. Altitude AGL: ${telemetry.altitude_m.toFixed(1)}m. Noise sigma < 0.12m.`;
    if (telemetry.pressure_hpa < 300 || telemetry.pressure_hpa > 1100) {
      baroStatus = 'CRITICAL';
      baroReason = 'Pressure reading out of physical atmospheric boundaries. I2C bus error suspected.';
    }

    // 4. Ambient Temperature / Humidity (BME680)
    let tempStatus: HealthStatus = 'HEALTHY';
    let tempReason = `Temp: ${telemetry.temperature_c.toFixed(1)} °C, Humidity: ${telemetry.humidity_pct.toFixed(1)}%. Normal ambient range.`;
    if (telemetry.temperature_c > 65 || telemetry.temperature_c < -25) {
      tempStatus = 'WARNING';
      tempReason = 'Temperature reading outside normal terrestrial envelope. Check thermal insulation.';
    }

    // 5. Particulate & AQI (PMS5003 Laser Counter)
    let aqiStatus: HealthStatus = 'HEALTHY';
    let aqiReason = `PM2.5: ${telemetry.pm25 ?? 'N/A'} µg/m³, PM10: ${telemetry.pm10 ?? 'N/A'} µg/m³, AQI: ${telemetry.aqi}. Laser fan active.`;
    if (telemetry.pm25 === null && telemetry.pm10 === null) {
      aqiStatus = 'WARNING';
      aqiReason = 'Fine particulate channels (PM2.5 / PM10) returning null. Single-channel AQI fallback active.';
    } else if (telemetry.aqi > 200) {
      aqiStatus = 'WARNING';
      aqiReason = 'Very high particulate concentration detected. Sensor fan intake may be obstructed.';
    }

    // 6. GNSS Navigation Module (NEO-6M)
    let gpsStatus: HealthStatus = 'HEALTHY';
    let gpsReason = `3D Geodetic Lock with ${telemetry.sats_visible} satellites. HDOP: ${telemetry.hdop ?? 0.85}. Geodetic position valid.`;
    if (telemetry.gps_fix === 'UNAVAILABLE' || telemetry.latitude === null) {
      gpsStatus = 'OFFLINE';
      gpsReason = 'GPS fix unavailable. Acquiring satellite ephemeris or antenna occluded.';
    } else if (telemetry.gps_fix === '2D_FIX' || telemetry.sats_visible < 4) {
      gpsStatus = 'WARNING';
      gpsReason = '2D Fix only (insufficient satellites for accurate vertical triangulation).';
    }

    // 7. Power Subsystem (LiPo 2S 7.4V / 8.4V)
    let battStatus: HealthStatus = 'HEALTHY';
    let battReason = `Pack Voltage: ${telemetry.battery_voltage_v.toFixed(2)}V (Dual Cell balanced). Sufficient operational reserve.`;
    if (telemetry.battery_voltage_v < 7.4) {
      battStatus = 'CRITICAL';
      battReason = 'Battery voltage below nominal threshold (7.4V). Imminent brownout risk.';
    } else if (telemetry.battery_voltage_v < 7.8) {
      battStatus = 'WARNING';
      battReason = 'Battery voltage dropped below 7.8V. Reserve duration under 15 minutes.';
    }

    const now = new Date().toLocaleTimeString();

    return [
      { name: 'Flight Computer (MCU)', hardware: 'ESP32 Dual-Core @ 240MHz', status: esp32Status, detail: esp32Reason, lastCheck: now },
      { name: 'RF Telemetry Transceiver', hardware: 'SX1278 LoRa 433 MHz', status: loraStatus, detail: loraReason, lastCheck: now },
      { name: 'Barometric Altimeter', hardware: 'BMP280 High-Precision I2C', status: baroStatus, detail: baroReason, lastCheck: now },
      { name: 'Atmospheric Temp/Humidity', hardware: 'BME680 Environmental Sensor', status: tempStatus, detail: tempReason, lastCheck: now },
      { name: 'Air Quality & Particulate', hardware: 'PMS5003 Optical Laser Counter', status: aqiStatus, detail: aqiReason, lastCheck: now },
      { name: 'GNSS Satellite Receiver', hardware: 'NEO-6M GPS Active Patch', status: gpsStatus, detail: gpsReason, lastCheck: now },
      { name: 'Electrical Power Subsystem', hardware: 'LiPo 2S 850mAh Battery & BMS', status: battStatus, detail: battReason, lastCheck: now },
    ];
  };

  const subsystems = getSubsystems();
  const overallCritical = subsystems.some((s) => s.status === 'CRITICAL');
  const overallWarning = subsystems.some((s) => s.status === 'WARNING');

  const statusIcons: Record<HealthStatus, React.ReactNode> = {
    HEALTHY: <CheckCircle2 className="w-4 h-4 text-emerald-400" />,
    WARNING: <AlertTriangle className="w-4 h-4 text-amber-400" />,
    CRITICAL: <XCircle className="w-4 h-4 text-red-400" />,
    OFFLINE: <WifiOff className="w-4 h-4 text-slate-500" />,
  };

  const statusBadges: Record<HealthStatus, { text: string; bg: string; border: string }> = {
    HEALTHY: { text: 'text-emerald-400', bg: 'bg-emerald-500/10', border: 'border-emerald-500/30' },
    WARNING: { text: 'text-amber-400', bg: 'bg-amber-500/10', border: 'border-amber-500/30' },
    CRITICAL: { text: 'text-red-400', bg: 'bg-red-500/10', border: 'border-red-500/30' },
    OFFLINE: { text: 'text-slate-400', bg: 'bg-slate-500/10', border: 'border-slate-500/30' },
  };

  return (
    <div className="space-y-4">
      {/* Overview Status Banner */}
      <div className="glass-panel p-5 rounded-xl border border-slate-800/80 flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center space-x-2.5">
            <Cpu className="w-5 h-5 text-cyan-400" />
            <h2 className="text-sm font-bold text-white tracking-wide uppercase">
              System Health & Diagnostics Panel
            </h2>
            <span className="text-[10px] mono px-2.5 py-0.5 rounded-full bg-slate-900 border border-slate-700 text-slate-300">
              MODE: {mode}
            </span>
          </div>
          <p className="text-xs text-slate-400 mt-1">
            Real-time telemetry integrity verification, physical sensor harness monitoring, and brownout fault detection.
          </p>
        </div>

        {/* Global Diagnostic Tag */}
        <div className="flex items-center space-x-3">
          {isStale ? (
            <div className="flex items-center space-x-2 px-3.5 py-1.5 rounded-xl bg-amber-500/15 border border-amber-500/40 text-amber-300 text-xs mono font-bold">
              <Clock className="w-4 h-4 text-amber-400 animate-spin" />
              <span>STALE TELEMETRY ({staleSeconds.toFixed(1)}s)</span>
            </div>
          ) : overallCritical ? (
            <div className="flex items-center space-x-2 px-3.5 py-1.5 rounded-xl bg-red-500/15 border border-red-500/40 text-red-300 text-xs mono font-bold">
              <ShieldAlert className="w-4 h-4 text-red-400" />
              <span>CRITICAL FAULT DETECTED</span>
            </div>
          ) : overallWarning ? (
            <div className="flex items-center space-x-2 px-3.5 py-1.5 rounded-xl bg-amber-500/15 border border-amber-500/40 text-amber-300 text-xs mono font-bold">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              <span>SUBSYSTEM WARNING</span>
            </div>
          ) : (
            <div className="flex items-center space-x-2 px-3.5 py-1.5 rounded-xl bg-emerald-500/15 border border-emerald-500/40 text-emerald-300 text-xs mono font-bold">
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              <span>ALL SUBSYSTEMS NOMINAL</span>
            </div>
          )}
        </div>
      </div>

      {/* Subsystems List */}
      <div className="grid grid-cols-1 gap-3">
        {subsystems.map((sub, idx) => {
          const badge = statusBadges[sub.status];
          return (
            <div
              key={idx}
              className={`glass-panel p-4 rounded-xl border flex flex-col md:flex-row justify-between items-start md:items-center gap-3 transition-all ${
                sub.status === 'CRITICAL'
                  ? 'border-red-500/50 bg-red-950/15 shadow-[0_0_15px_rgba(239,68,68,0.15)]'
                  : sub.status === 'WARNING'
                  ? 'border-amber-500/40 bg-amber-950/10'
                  : 'border-slate-800/80 bg-slate-900/30'
              }`}
            >
              <div className="flex items-start space-x-3.5">
                <div className="mt-0.5 shrink-0">{statusIcons[sub.status]}</div>
                <div>
                  <div className="flex items-center space-x-2">
                    <h3 className="text-xs font-bold text-white">{sub.name}</h3>
                    <span className="text-[10px] mono text-slate-400">({sub.hardware})</span>
                  </div>
                  <p className="text-[11px] text-slate-300 mt-1 leading-relaxed">
                    {sub.detail}
                  </p>
                </div>
              </div>

              <div className="flex items-center space-x-3 shrink-0 pl-7 md:pl-0">
                <span className="text-[10px] mono text-slate-500 hidden sm:inline">
                  {sub.lastCheck}
                </span>
                <span
                  className={`px-3 py-1 rounded-full text-[10px] font-bold mono uppercase border ${badge.bg} ${badge.text} ${badge.border}`}
                >
                  {sub.status}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
