import React from 'react';
import { TelemetryData, TelemetryPoint, MissionEvent, PhaseTimestamp, MissionPhase } from '../types/telemetry';
import { LineChart } from './LineChart';
import { 
  ArrowDown, 
  ArrowUp, 
  Layers, 
  Wind, 
  Activity, 
  Navigation, 
  Radio, 
  Cpu, 
  Zap, 
  Clock, 
  CheckCircle2, 
  MapPin,
  Compass
} from 'lucide-react';

interface OverviewTabProps {
  telemetry: TelemetryData;
  history: TelemetryPoint[];
  events: MissionEvent[];
  phaseTimestamps: Record<MissionPhase, PhaseTimestamp | null>;
}

export const OverviewTab: React.FC<OverviewTabProps> = ({
  telemetry,
  history,
  events,
  phaseTimestamps,
}) => {
  const altHistory = history.map((h) => h.alt);
  const tempHistory = history.map((h) => h.temp);
  const aqiHistory = history.map((h) => h.aqi);

  const isDescending = telemetry.vertical_velocity_ms < -0.2;
  const aqiRating = telemetry.aqi <= 50 ? 'Good' : telemetry.aqi <= 100 ? 'Moderate' : 'Unhealthy';

  // Mission Phase Sequence with transition timestamps
  const phasesList: { id: MissionPhase; label: string; desc: string }[] = [
    { id: 'STANDBY', label: 'Pre-launch', desc: 'Harness checks & calibration' },
    { id: 'DEPLOYED', label: 'Deployment', desc: '500m target apogee release' },
    { id: 'DESCENT', label: 'Descent', desc: 'Canopy stabilized descent' },
    { id: 'LANDING', label: 'Landing', desc: 'Impact detection & touchdown' },
    { id: 'RECOVERY', label: 'Recovery', desc: 'Beacon broadcast & retrieval' },
  ];

  // Helper to determine phase progression state
  const phaseOrder: MissionPhase[] = ['STANDBY', 'ARMED', 'DEPLOYED', 'DESCENT', 'LANDING', 'RECOVERY'];
  const currentPhaseIndex = phaseOrder.indexOf(telemetry.flight_phase);

  return (
    <div className="space-y-4">
      {/* SECTION C: MISSION PHASE TIMELINE WITH TIMESTAMPS */}
      <div className="glass-panel p-4 rounded-xl border border-slate-800/80">
        <div className="flex items-center justify-between pb-2 mb-3 border-b border-slate-800/60">
          <div className="flex items-center space-x-2">
            <Clock className="w-4 h-4 text-cyan-400" />
            <h3 className="text-xs font-bold text-white uppercase tracking-wider">
              Mission Phase Chronology & State Machine
            </h3>
          </div>
          <span className="text-[10px] mono text-slate-400">
            CURRENT: <span className="text-cyan-400 font-bold">{telemetry.flight_phase}</span>
          </span>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-5 gap-2.5">
          {phasesList.map((p, idx) => {
            const pIdx = phaseOrder.indexOf(p.id);
            const isCompleted = currentPhaseIndex > pIdx;
            const isCurrent = telemetry.flight_phase === p.id || (p.id === 'STANDBY' && telemetry.flight_phase === 'ARMED');
            const record = phaseTimestamps[p.id];

            return (
              <div
                key={p.id}
                className={`p-3 rounded-xl border transition-all relative ${
                  isCurrent
                    ? 'border-cyan-500/60 bg-cyan-950/20 shadow-[0_0_15px_rgba(0,209,255,0.15)]'
                    : isCompleted
                    ? 'border-emerald-500/30 bg-emerald-950/10'
                    : 'border-slate-800/60 bg-slate-900/30 opacity-70'
                }`}
              >
                <div className="flex justify-between items-center mb-1">
                  <span className="text-[9px] mono font-bold text-slate-400">PHASE 0{idx + 1}</span>
                  {isCurrent ? (
                    <span className="w-2 h-2 rounded-full bg-cyan-400 animate-ping" />
                  ) : isCompleted ? (
                    <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                  ) : (
                    <span className="w-2 h-2 rounded-full bg-slate-700" />
                  )}
                </div>

                <div className="text-xs font-bold text-white">{p.label}</div>
                <div className="text-[10px] text-slate-400 truncate mt-0.5">{p.desc}</div>

                <div className="mt-2 pt-1.5 border-t border-slate-800/60 text-[9px] mono flex justify-between">
                  <span className="text-slate-500">TIMESTAMP:</span>
                  <span className={isCurrent ? 'text-cyan-300 font-bold' : isCompleted ? 'text-emerald-400' : 'text-slate-500'}>
                    {record ? record.timeStr : isCurrent ? 'IN PROGRESS' : 'PENDING'}
                  </span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* SECTION B: PRIMARY TELEMETRY CARDS (8 Critical Measurements) */}
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-3">
        {/* 1. Barometric Altitude */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Altitude (AGL)</span>
            <span className="w-1.5 h-1.5 rounded-full bg-cyan-400" />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className="text-2xl font-bold mono text-white">{telemetry.altitude_m.toFixed(1)}</span>
            <span className="text-[10px] text-slate-400 mono">m</span>
          </div>
          <div className="text-[10px] mono text-slate-400 pt-1 border-t border-slate-800/60">
            {(telemetry.altitude_m * 3.28084).toFixed(0)} ft MSL
          </div>
        </div>

        {/* 2. Vertical Velocity */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Vertical Vel</span>
            <span className={`w-1.5 h-1.5 rounded-full ${isDescending ? 'bg-cyan-400' : 'bg-slate-400'}`} />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className={`text-2xl font-bold mono ${isDescending ? 'text-cyan-400' : 'text-slate-200'}`}>
              {Math.abs(telemetry.vertical_velocity_ms).toFixed(1)}
            </span>
            <span className="text-[10px] text-slate-400 mono">m/s</span>
          </div>
          <div className="text-[10px] mono text-slate-400 pt-1 border-t border-slate-800/60 flex items-center">
            {isDescending && <ArrowDown className="w-2.5 h-2.5 mr-0.5 text-cyan-400" />}
            <span>{isDescending ? 'Descent' : 'Neutral'}</span>
          </div>
        </div>

        {/* 3. Temperature */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Temperature</span>
            <span className="w-1.5 h-1.5 rounded-full bg-amber-400" />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className="text-2xl font-bold mono text-amber-300">{telemetry.temperature_c.toFixed(1)}</span>
            <span className="text-[10px] text-slate-400 mono">°C</span>
          </div>
          <div className="text-[10px] mono text-slate-400 pt-1 border-t border-slate-800/60">
            {(telemetry.temperature_c * 1.8 + 32).toFixed(1)} °F
          </div>
        </div>

        {/* 4. Pressure */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Pressure</span>
            <span className="w-1.5 h-1.5 rounded-full bg-purple-400" />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className="text-2xl font-bold mono text-purple-300">{telemetry.pressure_hpa.toFixed(1)}</span>
            <span className="text-[10px] text-slate-400 mono">hPa</span>
          </div>
          <div className="text-[10px] mono text-slate-400 pt-1 border-t border-slate-800/60">
            {(telemetry.pressure_hpa * 0.02953).toFixed(2)} inHg
          </div>
        </div>

        {/* 5. Air Quality (AQI) */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Air Quality</span>
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className="text-2xl font-bold mono text-emerald-300">{telemetry.aqi}</span>
            <span className="text-[10px] text-slate-400 mono">AQI</span>
          </div>
          <div className="text-[10px] mono text-emerald-400 pt-1 border-t border-slate-800/60 font-semibold">
            {aqiRating}
          </div>
        </div>

        {/* 6. PM2.5 & PM10 Particulate Readings */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>PM2.5 / PM10</span>
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
          </div>
          <div className="my-1.5 text-xs mono space-y-0.5">
            <div className="flex justify-between">
              <span className="text-slate-400">PM2.5:</span>
              <span className="text-white font-bold">{telemetry.pm25 ?? 'N/A'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-slate-400">PM10:</span>
              <span className="text-white font-bold">{telemetry.pm10 ?? 'N/A'}</span>
            </div>
          </div>
          <div className="text-[9px] mono text-slate-400 pt-1 border-t border-slate-800/60">
            µg/m³ Laser
          </div>
        </div>

        {/* 7. Battery Voltage */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>Battery 2S</span>
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-400" />
          </div>
          <div className="my-1.5 flex items-baseline space-x-1">
            <span className="text-2xl font-bold mono text-emerald-400">{telemetry.battery_voltage_v.toFixed(2)}</span>
            <span className="text-[10px] text-slate-400 mono">V</span>
          </div>
          <div className="text-[10px] mono text-slate-400 pt-1 border-t border-slate-800/60">
            Nominal 8.4V
          </div>
        </div>

        {/* 8. GPS Coordinates & Fix */}
        <div className="glass-panel p-3.5 rounded-xl flex flex-col justify-between">
          <div className="text-[9px] tracking-wider uppercase text-slate-400 font-semibold flex justify-between">
            <span>GPS Status</span>
            <span className="w-1.5 h-1.5 rounded-full bg-cyan-400" />
          </div>
          <div className="my-1 text-[11px] mono text-slate-200">
            {telemetry.latitude !== null && telemetry.longitude !== null ? (
              <>
                <div>{telemetry.latitude.toFixed(4)}°N</div>
                <div>{telemetry.longitude.toFixed(4)}°E</div>
              </>
            ) : (
              <div className="text-amber-400 font-bold text-[10px] mt-1">NO 3D FIX</div>
            )}
          </div>
          <div className="text-[9px] mono text-cyan-400 pt-1 border-t border-slate-800/60 font-medium">
            {telemetry.gps_fix} ({telemetry.sats_visible} SAT)
          </div>
        </div>
      </div>

      {/* SECTION D: FLIGHT STATUS, DESCENT LADDER, ATMOSPHERE & SUBSYSTEMS */}
      <div className="grid grid-cols-12 gap-3.5">
        {/* Flight Status & Altimeter Track */}
        <div className="col-span-12 lg:col-span-5 glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div className="flex justify-between items-center pb-2 border-b border-slate-800/60">
            <div className="flex items-center space-x-2">
              <Layers className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Descent Altimeter & Trajectory State
              </span>
            </div>
            <span className="text-[10px] mono px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 font-semibold">
              {telemetry.flight_phase}
            </span>
          </div>

          <div className="flex items-center space-x-6 my-3">
            {/* Altimeter Track */}
            <div className="relative w-12 h-44 bg-slate-950 rounded-xl border border-slate-800 flex flex-col justify-between py-2.5 px-1.5 shadow-inner">
              <div className="text-[9px] mono text-cyan-400 text-right font-bold">500m</div>
              <div className="text-[8px] mono text-slate-600 text-right">375m</div>
              <div className="text-[8px] mono text-slate-600 text-right">250m</div>
              <div className="text-[8px] mono text-slate-600 text-right">125m</div>
              <div className="text-[9px] mono text-emerald-400 text-right font-bold">0m</div>

              {/* Glowing CanSat Position Indicator */}
              <div
                style={{
                  bottom: `${Math.min(92, Math.max(5, (telemetry.altitude_m / 500) * 100))}%`,
                }}
                className="absolute left-1 right-1 h-3 bg-gradient-to-r from-cyan-400 to-blue-500 rounded-md shadow-[0_0_12px_#00D1FF] flex items-center justify-center transition-all duration-300"
              >
                <div className="w-1.5 h-1.5 rounded-full bg-white shadow-sm" />
              </div>
            </div>

            {/* Trajectory Details */}
            <div className="flex-1 space-y-3">
              <div>
                <div className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold">
                  Altitude Relative to Ground
                </div>
                <div className="text-3xl font-bold mono text-white">
                  {telemetry.altitude_m.toFixed(1)} <span className="text-sm font-normal text-slate-400">m AGL</span>
                </div>
                <div className="text-[10px] mono text-slate-400 mt-0.5">
                  Target Ejection: <span className="text-cyan-400 font-semibold">500.0 m</span>
                </div>
              </div>

              <div>
                <div className="text-[10px] text-slate-400 uppercase tracking-wider font-semibold">
                  Descent Velocity & State
                </div>
                <div className="text-xl font-bold mono text-cyan-300">
                  {Math.abs(telemetry.vertical_velocity_ms).toFixed(2)} <span className="text-xs text-slate-400 font-normal">m/s</span>
                </div>
                <div className="text-[10px] mono text-slate-400 mt-0.5">
                  Chute Drag State: <span className="text-emerald-400 font-semibold">
                    {Math.abs(telemetry.vertical_velocity_ms) > 0.5 ? 'AERODYNAMIC EQUILIBRIUM' : 'GROUND / PAD'}
                  </span>
                </div>
              </div>

              {/* Predicted Landing Location */}
              <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800/60 text-[10px] mono">
                <div className="text-slate-400 font-semibold flex items-center space-x-1">
                  <MapPin className="w-3 h-3 text-cyan-400" />
                  <span>PREDICTED TOUCHDOWN SITE</span>
                </div>
                <div className="text-cyan-300 font-bold mt-0.5">
                  {telemetry.predicted_lat?.toFixed(5)}° N, {telemetry.predicted_lon?.toFixed(5)}° E
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Atmospheric Matrix */}
        <div className="col-span-12 lg:col-span-4 glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div className="flex justify-between items-center pb-2 border-b border-slate-800/60">
            <div className="flex items-center space-x-2">
              <Wind className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Atmospheric Sounding
              </span>
            </div>
            <span className="text-[10px] text-slate-400 mono">BME680 Sensor</span>
          </div>

          <div className="grid grid-cols-2 gap-2 my-2.5">
            <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="text-[9px] uppercase tracking-wider text-slate-400 font-medium">Air Density (ρ)</div>
              <div className="text-base font-bold mono text-cyan-300 mt-0.5">1.192 <span className="text-[10px] font-normal text-slate-400">kg/m³</span></div>
            </div>
            <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="text-[9px] uppercase tracking-wider text-slate-400 font-medium">Relative Humidity</div>
              <div className="text-base font-bold mono text-blue-300 mt-0.5">{telemetry.humidity_pct.toFixed(1)} <span className="text-[10px] font-normal text-slate-400">%</span></div>
            </div>
            <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="text-[9px] uppercase tracking-wider text-slate-400 font-medium">ISA Lapse Reference</div>
              <div className="text-base font-bold mono text-amber-300 mt-0.5">-6.5 <span className="text-[10px] font-normal text-slate-400">°C/km</span></div>
            </div>
            <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="text-[9px] uppercase tracking-wider text-slate-400 font-medium">Laser Particulate</div>
              <div className="text-base font-bold mono text-emerald-300 mt-0.5">{telemetry.aqi} <span className="text-[10px] font-normal text-slate-400">AQI</span></div>
            </div>
          </div>

          <div className="p-2.5 rounded-lg bg-slate-900/40 border border-slate-800/60 text-[10px] mono text-slate-400 flex justify-between items-center">
            <span>Barometric Pressure Head:</span>
            <span className="text-cyan-400 font-bold">{telemetry.pressure_hpa.toFixed(1)} hPa</span>
          </div>
        </div>

        {/* Subsystems Quick Matrix */}
        <div className="col-span-12 lg:col-span-3 glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div className="flex justify-between items-center pb-2 border-b border-slate-800/60">
            <div className="flex items-center space-x-2">
              <Activity className="w-4 h-4 text-emerald-400" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Subsystems Status
              </span>
            </div>
            <span className="text-[10px] text-emerald-400 mono font-bold">ALL NOMINAL</span>
          </div>

          <div className="space-y-2 my-2 text-xs mono">
            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="flex items-center space-x-2">
                <Cpu className="w-3.5 h-3.5 text-slate-400" />
                <span className="text-slate-300">ESP32 MCU</span>
              </div>
              <span className="text-emerald-400 font-bold text-[11px]">240 MHz OK</span>
            </div>

            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="flex items-center space-x-2">
                <Radio className="w-3.5 h-3.5 text-slate-400" />
                <span className="text-slate-300">SX1278 LoRa</span>
              </div>
              <span className="text-emerald-400 font-bold text-[11px]">433 MHz TX</span>
            </div>

            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="flex items-center space-x-2">
                <Navigation className="w-3.5 h-3.5 text-slate-400" />
                <span className="text-slate-300">NEO-6M GPS</span>
              </div>
              <span className="text-cyan-400 font-bold text-[11px]">3D FIX (11 SAT)</span>
            </div>

            <div className="flex items-center justify-between p-2 rounded-lg bg-slate-900/50 border border-slate-800/60">
              <div className="flex items-center space-x-2">
                <Zap className="w-3.5 h-3.5 text-slate-400" />
                <span className="text-slate-300">LiPo 2S Power</span>
              </div>
              <span className="text-emerald-400 font-bold text-[11px]">{telemetry.battery_voltage_v.toFixed(2)}V NOM</span>
            </div>
          </div>

          <div className="text-[9px] mono text-slate-500 text-center">
            ACTIVE CARRIER: SX1278 / 433.0 MHz
          </div>
        </div>
      </div>

      {/* CHARTS ROW */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-3.5">
        <div className="glass-panel p-3.5 rounded-xl">
          <div className="flex justify-between items-center text-xs font-semibold text-slate-200 mb-2">
            <span>Altitude Profile (m AGL)</span>
            <span className="text-[10px] mono text-cyan-400 font-medium">Target: 500m</span>
          </div>
          <LineChart
            data={altHistory}
            minVal={0}
            maxVal={520}
            color="#00D1FF"
            unit="m"
            targetLineVal={500}
          />
        </div>

        <div className="glass-panel p-3.5 rounded-xl">
          <div className="flex justify-between items-center text-xs font-semibold text-slate-200 mb-2">
            <span>Atmospheric Temperature (°C)</span>
            <span className="text-[10px] mono text-amber-300 font-medium">
              {telemetry.temperature_c.toFixed(1)}°C
            </span>
          </div>
          <LineChart
            data={tempHistory}
            minVal={15}
            maxVal={35}
            color="#FFB547"
            unit="°C"
          />
        </div>

        <div className="glass-panel p-3.5 rounded-xl">
          <div className="flex justify-between items-center text-xs font-semibold text-slate-200 mb-2">
            <span>Air Quality Index (AQI)</span>
            <span className="text-[10px] mono text-emerald-400 font-medium">
              {telemetry.aqi} AQI
            </span>
          </div>
          <LineChart
            data={aqiHistory}
            minVal={20}
            maxVal={100}
            color="#22C98B"
            unit="AQI"
          />
        </div>
      </div>
    </div>
  );
};
