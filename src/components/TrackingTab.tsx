import React, { useRef, useEffect } from 'react';
import { TelemetryData } from '../types/telemetry';
import { 
  Compass, 
  Navigation, 
  MapPin, 
  AlertTriangle, 
  ShieldCheck, 
  Radio, 
  Flag,
  Crosshair,
  Volume2
} from 'lucide-react';

interface TrackingTabProps {
  telemetry: TelemetryData;
}

export const TrackingTab: React.FC<TrackingTabProps> = ({ telemetry }) => {
  const radarRef = useRef<HTMLCanvasElement>(null);
  const mapRef = useRef<HTMLCanvasElement>(null);

  // Radar Animation Loop
  useEffect(() => {
    const canvas = radarRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let angle = 0;
    let animId: number;

    const renderRadar = () => {
      const w = 300;
      const h = 300;
      const cx = w / 2;
      const cy = h / 2;

      ctx.clearRect(0, 0, w, h);

      // Rotating radar beam
      angle = (angle + 0.035) % (Math.PI * 2);
      ctx.beginPath();
      ctx.moveTo(cx, cy);
      ctx.arc(cx, cy, 140, angle - 0.28, angle);
      ctx.closePath();
      const grad = ctx.createRadialGradient(cx, cy, 0, cx, cy, 140);
      grad.addColorStop(0, 'rgba(0, 209, 255, 0.35)');
      grad.addColorStop(0.7, 'rgba(0, 209, 255, 0.12)');
      grad.addColorStop(1, 'rgba(0, 209, 255, 0)');
      ctx.fillStyle = grad;
      ctx.fill();

      // CanSat target blip (only if coordinates/altitude exist)
      if (telemetry.latitude !== null) {
        const distRatio = Math.min(1, telemetry.altitude_m / 500);
        const px = cx + Math.cos(angle - 0.6) * (distRatio * 115 + 12);
        const py = cy + Math.sin(angle - 0.6) * (distRatio * 115 + 12);

        ctx.beginPath();
        ctx.arc(px, py, 8, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(0, 209, 255, 0.25)';
        ctx.fill();

        ctx.beginPath();
        ctx.arc(px, py, 4, 0, Math.PI * 2);
        ctx.fillStyle = '#00D1FF';
        ctx.fill();
        ctx.strokeStyle = '#FFFFFF';
        ctx.lineWidth = 1.5;
        ctx.stroke();

        ctx.fillStyle = '#00D1FF';
        ctx.font = '600 10px monospace';
        ctx.fillText('AETHER-01', px + 9, py - 4);
      }

      animId = requestAnimationFrame(renderRadar);
    };

    renderRadar();

    return () => {
      cancelAnimationFrame(animId);
    };
  }, [telemetry.altitude_m, telemetry.latitude]);

  // Tactical Coordinate Map Renderer
  useEffect(() => {
    const canvas = mapRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const dpr = window.devicePixelRatio || 1;
    const w = canvas.parentElement?.clientWidth || 400;
    const h = 260;
    canvas.width = w * dpr;
    canvas.height = h * dpr;
    ctx.scale(dpr, dpr);

    ctx.clearRect(0, 0, w, h);

    // Grid lines (Geodetic Coordinate Grid)
    ctx.strokeStyle = 'rgba(51, 71, 102, 0.35)';
    ctx.lineWidth = 1;
    for (let x = 20; x < w; x += 40) {
      ctx.beginPath();
      ctx.moveTo(x, 10);
      ctx.lineTo(x, h - 10);
      ctx.stroke();
    }
    for (let y = 20; y < h; y += 40) {
      ctx.beginPath();
      ctx.moveTo(10, y);
      ctx.lineTo(w - 10, y);
      ctx.stroke();
    }

    // Launch Pad Center [w/4, h*0.75]
    const padX = w * 0.25;
    const padY = h * 0.75;

    ctx.beginPath();
    ctx.arc(padX, padY, 6, 0, Math.PI * 2);
    ctx.fillStyle = '#5271FF';
    ctx.fill();
    ctx.strokeStyle = '#FFFFFF';
    ctx.lineWidth = 1.5;
    ctx.stroke();
    ctx.fillStyle = '#5271FF';
    ctx.font = '600 10px monospace';
    ctx.fillText('LAUNCH PAD (DTU)', padX - 45, padY + 16);

    // Predicted Touchdown Target [w*0.75, h*0.35]
    const predX = w * 0.72;
    const predY = h * 0.32;

    ctx.beginPath();
    ctx.arc(predX, predY, 6, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(245, 158, 11, 0.4)';
    ctx.fill();
    ctx.strokeStyle = '#F59E0B';
    ctx.lineWidth = 1.5;
    ctx.setLineDash([3, 3]);
    ctx.stroke();
    ctx.setLineDash([]);
    ctx.fillStyle = '#F59E0B';
    ctx.font = '600 9px monospace';
    ctx.fillText('PREDICTED LANDING', predX - 40, predY - 10);

    // Trajectory dashed vector from pad to predicted
    ctx.strokeStyle = 'rgba(245, 158, 11, 0.35)';
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(padX, padY);
    ctx.lineTo(predX, predY);
    ctx.stroke();
    ctx.setLineDash([]);

    // Actual CanSat Current Location (if GPS available)
    if (telemetry.latitude !== null) {
      const progress = Math.min(1.0, Math.max(0.0, 1.0 - telemetry.altitude_m / 500));
      const curX = padX + (predX - padX) * progress;
      const curY = padY + (predY - padY) * progress;

      // Trajectory solid track up to current position
      ctx.strokeStyle = '#00D1FF';
      ctx.lineWidth = 2;
      ctx.beginPath();
      ctx.moveTo(padX, padY);
      ctx.lineTo(curX, curY);
      ctx.stroke();

      // Current probe blip
      ctx.beginPath();
      ctx.arc(curX, curY, 8, 0, Math.PI * 2);
      ctx.fillStyle = 'rgba(0, 209, 255, 0.3)';
      ctx.fill();

      ctx.beginPath();
      ctx.arc(curX, curY, 4, 0, Math.PI * 2);
      ctx.fillStyle = '#00D1FF';
      ctx.fill();
      ctx.strokeStyle = '#FFFFFF';
      ctx.lineWidth = 1.5;
      ctx.stroke();

      ctx.fillStyle = '#00D1FF';
      ctx.font = 'bold 10px monospace';
      ctx.fillText('CANSAT CURRENT', curX + 10, curY + 4);

      // If landed, mark Actual Touchdown Site
      if (telemetry.landing_lat !== null) {
        ctx.beginPath();
        ctx.arc(curX, curY, 12, 0, Math.PI * 2);
        ctx.strokeStyle = '#10B981';
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.fillStyle = '#10B981';
        ctx.font = 'bold 10px monospace';
        ctx.fillText('✓ ACTUAL LANDING', curX - 45, curY + 22);
      }
    }
  }, [telemetry.altitude_m, telemetry.latitude, telemetry.landing_lat]);

  const hasGps = telemetry.gps_fix !== 'UNAVAILABLE' && telemetry.latitude !== null;
  const slantRange = Math.sqrt(telemetry.altitude_m * telemetry.altitude_m + 2500).toFixed(1);

  return (
    <div className="space-y-4">
      <div className="grid grid-cols-12 gap-3.5">
        {/* Tactical Radar Scope (Left) */}
        <div className="col-span-12 lg:col-span-6 glass-panel p-4 rounded-xl flex flex-col items-center justify-between">
          <div className="w-full flex justify-between items-center pb-2 border-b border-slate-800/60">
            <div className="flex items-center space-x-2">
              <Compass className="w-4 h-4 text-cyan-400" />
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Tactical Slant Range & Radar Scope
              </span>
            </div>
            <span className="text-[10px] mono px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 font-semibold">
              RANGE: 500m
            </span>
          </div>

          <div className="my-3 relative w-72 h-72 rounded-full border border-cyan-500/30 bg-[#050811] flex items-center justify-center overflow-hidden radar-grid shadow-[0_0_25px_rgba(0,209,255,0.08)]">
            <canvas ref={radarRef} width={300} height={300} className="absolute inset-0" />
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
              <div className="w-56 h-56 rounded-full border border-cyan-500/20" />
              <div className="w-36 h-36 rounded-full border border-cyan-500/20 absolute" />
              <div className="w-16 h-16 rounded-full border border-cyan-500/20 absolute" />
              <div className="w-full h-px bg-cyan-500/20 absolute" />
              <div className="h-full w-px bg-cyan-500/20 absolute" />
            </div>
          </div>

          <div className="w-full flex justify-between text-xs mono text-slate-400 pt-2 border-t border-slate-800/60">
            <span>GROUND BASE: [0, 0]</span>
            <span className="text-cyan-400 font-medium">AZ: 042° | EL: 74°</span>
            <span>SLANT: {slantRange} m</span>
          </div>
        </div>

        {/* Tactical Geodetic Map (Right) */}
        <div className="col-span-12 lg:col-span-6 glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div>
            <div className="w-full flex justify-between items-center pb-2 border-b border-slate-800/60 mb-3">
              <div className="flex items-center space-x-2">
                <Navigation className="w-4 h-4 text-emerald-400" />
                <span className="text-xs font-bold text-white uppercase tracking-wider">
                  Tactical Geodetic Map & Recovery Trajectory
                </span>
              </div>
              <span className="text-[10px] mono text-slate-400">
                CAMPUS REF: DTU DELHI
              </span>
            </div>

            <div className="w-full rounded-xl bg-slate-950 border border-slate-800/80 overflow-hidden relative">
              <canvas ref={mapRef} className="w-full block" style={{ height: '260px' }} />
            </div>
          </div>

          <div className="flex justify-between items-center text-[10px] mono text-slate-400 pt-2.5 mt-2 border-t border-slate-800/60">
            <span className="flex items-center space-x-1.5">
              <span className="w-2 h-2 rounded-full bg-blue-500" />
              <span>Launch Base</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <span className="w-2 h-2 rounded-full bg-amber-400 border border-amber-400" />
              <span>Predicted Site</span>
            </span>
            <span className="flex items-center space-x-1.5">
              <span className="w-2 h-2 rounded-full bg-cyan-400" />
              <span>CanSat Position</span>
            </span>
          </div>
        </div>
      </div>

      {/* GNSS Details & Recovery Guidance Matrix */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3.5">
        {/* Actual GPS Location */}
        <div className="glass-panel p-4 rounded-xl">
          <div className="flex justify-between items-center pb-2 mb-2 border-b border-slate-800/60">
            <span className="text-xs font-bold text-white uppercase tracking-wider">
              Actual CanSat GPS Position
            </span>
            <span
              className={`text-[10px] mono font-bold px-2 py-0.5 rounded-full border ${
                hasGps
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
                  : 'bg-red-500/10 border-red-500/30 text-red-400'
              }`}
            >
              {telemetry.gps_fix}
            </span>
          </div>

          {hasGps ? (
            <div className="space-y-2 text-xs mono">
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Latitude:</span>
                <span className="text-white font-bold">{telemetry.latitude?.toFixed(6)}° N</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Longitude:</span>
                <span className="text-white font-bold">{telemetry.longitude?.toFixed(6)}° E</span>
              </div>
              <div className="flex justify-between text-[10px] text-slate-400 px-1">
                <span>SATELLITES: {telemetry.sats_visible} LOCK</span>
                <span>HDOP: {telemetry.hdop ?? 0.85}</span>
              </div>
            </div>
          ) : (
            <div className="p-4 rounded-lg bg-red-950/20 border border-red-500/30 text-center space-y-1">
              <AlertTriangle className="w-6 h-6 text-red-400 mx-auto" />
              <div className="text-xs font-bold text-red-400">GPS FIX UNAVAILABLE</div>
              <div className="text-[10px] text-slate-400">
                Satellite ephemeris search in progress. Antenna occluded or awaiting lock.
              </div>
            </div>
          )}
        </div>

        {/* Predicted Landing Location */}
        <div className="glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex justify-between items-center pb-2 mb-2 border-b border-slate-800/60">
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Predicted Landing Site (Drift Model)
              </span>
              <span className="text-[10px] mono text-amber-400 font-bold">PREDICTED</span>
            </div>

            <div className="space-y-2 text-xs mono">
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Predicted Lat:</span>
                <span className="text-amber-300 font-bold">{telemetry.predicted_lat?.toFixed(6)}° N</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Predicted Lon:</span>
                <span className="text-amber-300 font-bold">{telemetry.predicted_lon?.toFixed(6)}° E</span>
              </div>
            </div>
          </div>

          <div className="text-[9px] mono text-slate-500 mt-2">
            NOTICE: Predicted coordinates represent dynamic wind-drift extrapolation and are never confused with actual touchdown data.
          </div>
        </div>

        {/* Recovery Status */}
        <div className="glass-panel p-4 rounded-xl flex flex-col justify-between">
          <div>
            <div className="flex justify-between items-center pb-2 mb-2 border-b border-slate-800/60">
              <span className="text-xs font-bold text-white uppercase tracking-wider">
                Recovery Beacon & Retrieval Vector
              </span>
              <span
                className={`text-[10px] mono font-bold px-2 py-0.5 rounded-full border ${
                  telemetry.recovery_beacon_active
                    ? 'bg-purple-500/10 border-purple-500/30 text-purple-400 animate-pulse'
                    : 'bg-slate-900 border-slate-700 text-slate-500'
                }`}
              >
                {telemetry.recovery_beacon_active ? 'BEACON ACTIVE' : 'STANDBY'}
              </span>
            </div>

            <div className="space-y-2 text-xs mono">
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Azimuth Bearing:</span>
                <span className="text-cyan-400 font-bold">042° NE from Pad</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-900/50 border border-slate-800/60 flex justify-between">
                <span className="text-slate-400">Ground Distance:</span>
                <span className="text-emerald-400 font-bold">186.4 metres</span>
              </div>
            </div>
          </div>

          <div className="p-2.5 rounded-lg bg-purple-950/20 border border-purple-500/30 text-[10px] mono text-purple-300 flex items-center space-x-2 mt-2">
            <Volume2 className="w-4 h-4 text-purple-400 shrink-0" />
            <span>Audio Buzzer 2.4 kHz beacon pinging post-touchdown for field retrieval.</span>
          </div>
        </div>
      </div>
    </div>
  );
};
