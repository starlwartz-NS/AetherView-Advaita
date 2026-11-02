import React, { useState } from 'react';
import { TelemetryData, TelemetryPoint } from '../types/telemetry';
import { LineChart } from './LineChart';
import { Activity, Download, Clock, ShieldCheck, Zap } from 'lucide-react';

interface TelemetryTabProps {
  telemetry: TelemetryData;
  history: TelemetryPoint[];
  staleSeconds: number;
}

export const TelemetryTab: React.FC<TelemetryTabProps> = ({ telemetry, history, staleSeconds }) => {
  const [horizon, setHorizon] = useState<'30s' | '60s' | '180s' | 'ALL'>('60s');

  const sliceCount = horizon === '30s' ? 60 : horizon === '60s' ? 120 : horizon === '180s' ? 360 : history.length;
  const filteredHistory = history.slice(-sliceCount);

  // Compute min, max, avg for key channels
  const calcStats = (vals: number[]) => {
    if (vals.length === 0) return { min: 0, max: 0, avg: 0 };
    const min = Math.min(...vals);
    const max = Math.max(...vals);
    const avg = vals.reduce((a, b) => a + b, 0) / vals.length;
    return { min, max, avg };
  };

  const altStats = calcStats(filteredHistory.map((p) => p.alt));
  const tempStats = calcStats(filteredHistory.map((p) => p.temp));
  const pressStats = calcStats(filteredHistory.map((p) => p.pressure));
  const velStats = calcStats(filteredHistory.map((p) => Math.abs(p.vel)));
  const aqiStats = calcStats(filteredHistory.map((p) => p.aqi));
  const battStats = calcStats(filteredHistory.map((p) => p.batt));

  // CSV Export
  const exportCSV = () => {
    let csv = 'packet_id,timestamp_met,phase,altitude_m,velocity_ms,temperature_c,pressure_hpa,humidity_pct,aqi,pm25_ugm3,pm10_ugm3,battery_v,rssi_dbm,snr_db\n';
    history.forEach((p) => {
      csv += `${p.packetId},"${p.time}","${p.phase}",${p.alt.toFixed(2)},${p.vel.toFixed(2)},${p.temp.toFixed(2)},${p.pressure.toFixed(2)},${p.humidity.toFixed(2)},${p.aqi},${p.pm25 ?? ''},${p.pm10 ?? ''},${p.batt.toFixed(2)},${p.rssi},${p.snr}\n`;
    });

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `AetherView_Advaita_Telemetry_${Date.now()}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Freshness indicator logic
  const isFresh = staleSeconds < 1.5;
  const isStale = staleSeconds >= 3.0;

  return (
    <div className="space-y-4">
      {/* Header with Freshness, Stats & Range Controls */}
      <div className="flex flex-col lg:flex-row justify-between items-start lg:items-center gap-3 pb-2.5 border-b border-slate-800/60">
        <div className="flex items-center space-x-3">
          <Activity className="w-5 h-5 text-cyan-400" />
          <div>
            <h2 className="text-sm font-bold text-white uppercase tracking-wider">
              Live Multi-Channel Telemetry Waveforms
            </h2>
            <div className="text-[10px] text-slate-400 mono">
              Sampling epoch: 500ms • Buffer: {history.length} frames
            </div>
          </div>
        </div>

        {/* Data Freshness Indicator & Controls */}
        <div className="flex items-center space-x-3 flex-wrap gap-y-2">
          {/* Freshness Badge */}
          <div className="flex items-center space-x-2 px-3 py-1 rounded-xl bg-slate-900/60 border border-slate-800 text-[10px] mono">
            <span className="text-slate-400">DATA FRESHNESS:</span>
            <span className="flex items-center space-x-1 font-bold">
              <span
                className={`w-2 h-2 rounded-full ${
                  isFresh
                    ? 'bg-emerald-400 animate-pulse'
                    : isStale
                    ? 'bg-amber-400'
                    : 'bg-cyan-400'
                }`}
              />
              <span className={isFresh ? 'text-emerald-400' : isStale ? 'text-amber-400' : 'text-cyan-400'}>
                {isFresh ? 'FRESH (<1.5s)' : isStale ? `STALE (${staleSeconds.toFixed(1)}s)` : 'NOMINAL'}
              </span>
            </span>
          </div>

          {/* Time Window Buttons */}
          <div className="flex items-center space-x-1 bg-slate-900/60 p-1 rounded-xl border border-slate-800">
            <span className="text-[10px] mono text-slate-500 px-2 font-medium">WINDOW</span>
            {(['30s', '60s', '180s', 'ALL'] as const).map((h) => (
              <button
                key={h}
                onClick={() => setHorizon(h)}
                className={`px-2.5 py-1 text-[10px] mono rounded-lg font-bold transition-all ${
                  horizon === h
                    ? 'bg-cyan-500 text-slate-950 shadow-[0_0_10px_rgba(0,209,255,0.4)]'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                {h}
              </button>
            ))}
          </div>

          {/* CSV Export */}
          <button
            onClick={exportCSV}
            className="px-3.5 py-1.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold transition-all flex items-center space-x-1.5 shadow-[0_0_12px_rgba(16,185,129,0.3)] active:scale-95"
          >
            <Download className="w-3.5 h-3.5" />
            <span>EXPORT CSV</span>
          </button>
        </div>
      </div>

      {/* 6 Scrolling Graphs with Min/Max/Avg Stats */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-3.5">
        {/* 1. Altitude */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Altitude Profile (m AGL)</span>
            <span className="text-cyan-400 mono font-bold">{telemetry.altitude_m.toFixed(1)} m</span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN: {altStats.min.toFixed(1)}m</span>
            <span>MAX: {altStats.max.toFixed(1)}m</span>
            <span>AVG: {altStats.avg.toFixed(1)}m</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.alt)}
            minVal={0}
            maxVal={520}
            color="#00D1FF"
            unit="m"
            height={140}
            targetLineVal={500}
          />
        </div>

        {/* 2. Vertical Velocity */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Vertical Velocity Profile (m/s)</span>
            <span className="text-blue-400 mono font-bold">{telemetry.vertical_velocity_ms.toFixed(1)} m/s</span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN: {velStats.min.toFixed(1)} m/s</span>
            <span>MAX: {velStats.max.toFixed(1)} m/s</span>
            <span>AVG: {velStats.avg.toFixed(1)} m/s</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.vel)}
            minVal={-12}
            maxVal={4}
            color="#5271FF"
            unit="m/s"
            height={140}
          />
        </div>

        {/* 3. Temperature */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Ambient Temperature (°C)</span>
            <span className="text-amber-300 mono font-bold">{telemetry.temperature_c.toFixed(1)} °C</span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN: {tempStats.min.toFixed(1)}°C</span>
            <span>MAX: {tempStats.max.toFixed(1)}°C</span>
            <span>AVG: {tempStats.avg.toFixed(1)}°C</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.temp)}
            minVal={15}
            maxVal={35}
            color="#FFB547"
            unit="°C"
            height={140}
          />
        </div>

        {/* 4. Pressure */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Barometric Pressure (hPa)</span>
            <span className="text-purple-300 mono font-bold">{telemetry.pressure_hpa.toFixed(1)} hPa</span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN: {pressStats.min.toFixed(1)} hPa</span>
            <span>MAX: {pressStats.max.toFixed(1)} hPa</span>
            <span>AVG: {pressStats.avg.toFixed(1)} hPa</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.pressure)}
            minVal={940}
            maxVal={1025}
            color="#C084FC"
            unit="hPa"
            height={140}
          />
        </div>

        {/* 5. AQI & Laser Particulate (PM2.5 / PM10) */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Air Quality Index & Laser Particulate</span>
            <span className="text-emerald-400 mono font-bold">
              AQI: {telemetry.aqi} | PM2.5: {telemetry.pm25 ?? 'N/A'} µg/m³
            </span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN AQI: {aqiStats.min.toFixed(0)}</span>
            <span>MAX AQI: {aqiStats.max.toFixed(0)}</span>
            <span>AVG AQI: {aqiStats.avg.toFixed(0)}</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.aqi)}
            minVal={20}
            maxVal={100}
            color="#34D399"
            unit="AQI"
            height={140}
          />
        </div>

        {/* 6. Battery Voltage History */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center text-xs font-bold text-slate-200 mb-1">
            <span>Battery 2S Cell Pack (V)</span>
            <span className="text-emerald-400 mono font-bold">{telemetry.battery_voltage_v.toFixed(2)} V</span>
          </div>
          <div className="text-[9px] mono text-slate-400 mb-2 flex space-x-4">
            <span>MIN: {battStats.min.toFixed(2)}V</span>
            <span>MAX: {battStats.max.toFixed(2)}V</span>
            <span>NOMINAL: 8.40V</span>
          </div>
          <LineChart
            data={filteredHistory.map((p) => p.batt)}
            minVal={7.4}
            maxVal={8.5}
            color="#22C98B"
            unit="V"
            height={140}
          />
        </div>
      </div>
    </div>
  );
};
