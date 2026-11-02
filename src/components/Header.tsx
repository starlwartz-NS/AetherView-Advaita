import React, { useEffect, useState } from 'react';
import { Play, Pause, RotateCcw, Clock, Radio, Activity } from 'lucide-react';
import { TelemetryData, OperatingMode } from '../types/telemetry';

interface HeaderProps {
  telemetry: TelemetryData;
  met: number;
  mode: OperatingMode;
  connected: boolean;
  isRunning: boolean;
  isPaused: boolean;
  onToggleSim: () => void;
  onResetSim: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  telemetry,
  met,
  mode,
  connected,
  isRunning,
  isPaused,
  onToggleSim,
  onResetSim,
}) => {
  const [utcTime, setUtcTime] = useState('');
  const [locTime, setLocTime] = useState('');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setUtcTime(now.toUTCString().split(' ')[4] + ' UTC');
      setLocTime(now.toLocaleTimeString([], { hour12: false }));
    };
    updateTime();
    const timer = setInterval(updateTime, 1000);
    return () => clearInterval(timer);
  }, []);

  const mins = Math.floor(met / 60).toString().padStart(2, '0');
  const secs = Math.floor(met % 60).toString().padStart(2, '0');
  const tenths = Math.floor((met % 1) * 10);
  const metDisplay = `T+${mins}:${secs}.${tenths}`;

  const phaseColors: Record<string, { bg: string; text: string; border: string; glow: string }> = {
    STANDBY: { bg: 'bg-slate-500/15', text: 'text-slate-300', border: 'border-slate-500/30', glow: '' },
    ARMED: { bg: 'bg-amber-500/15', text: 'text-amber-400', border: 'border-amber-500/40', glow: 'shadow-[0_0_12px_rgba(245,158,11,0.2)]' },
    DEPLOYED: { bg: 'bg-cyan-500/15', text: 'text-[#00D1FF]', border: 'border-cyan-500/50', glow: 'shadow-[0_0_12px_rgba(0,209,255,0.3)]' },
    DESCENT: { bg: 'bg-blue-500/15', text: 'text-blue-400', border: 'border-blue-500/50', glow: 'shadow-[0_0_12px_rgba(59,130,246,0.3)]' },
    LANDING: { bg: 'bg-emerald-500/15', text: 'text-emerald-400', border: 'border-emerald-500/50', glow: 'shadow-[0_0_12px_rgba(16,185,129,0.3)]' },
    RECOVERY: { bg: 'bg-purple-500/15', text: 'text-purple-400', border: 'border-purple-500/50', glow: 'shadow-[0_0_12px_rgba(168,85,247,0.3)]' },
  };

  const currentPhaseStyle = phaseColors[telemetry.flight_phase] || phaseColors.STANDBY;

  return (
    <header className="h-14 min-h-[56px] px-5 flex items-center justify-between bg-[#070c17]/90 backdrop-blur-md border-b border-slate-800/80 shrink-0 select-none z-10">
      {/* Brand & Mission Status */}
      <div className="flex items-center space-x-3.5">
        <div className="flex items-center space-x-1.5">
          <img
            src="/aetherview-advaita-logo.png"
            alt=""
            aria-hidden="true"
            className="w-7 h-7 rounded-full object-cover"
          />
          <span className="font-bold text-white text-xs tracking-wide whitespace-nowrap">AetherView Advaita</span>
          <span className="text-slate-500 text-xs">/</span>
          <span className="text-slate-400 text-xs font-medium tracking-wide whitespace-nowrap">MISSION CONTROL</span>
        </div>

        <div className="h-4 w-px bg-slate-800 hidden sm:block" />

        {/* Phase Pill */}
        <div
          className={`flex items-center space-x-1.5 px-2.5 py-1 rounded-full text-[11px] font-semibold mono border ${currentPhaseStyle.bg} ${currentPhaseStyle.text} ${currentPhaseStyle.border} ${currentPhaseStyle.glow}`}
        >
          <span className="w-1.5 h-1.5 rounded-full bg-current animate-pulse" />
          <span>{telemetry.flight_phase}</span>
        </div>

        {/* MET Timer */}
        <div className="flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-slate-900/60 border border-slate-800/60 mono text-xs font-semibold text-slate-200">
          <Clock className="w-3.5 h-3.5 text-[#00D1FF]" />
          <span className="text-slate-400 text-[10px]">MET</span>
          <span className="text-[#00D1FF]">{metDisplay}</span>
        </div>
      </div>

      {/* Center/Right Status & Controls */}
      <div className="flex items-center space-x-3">
        {/* Operating Mode Indicator */}
        <div
          className={`flex items-center space-x-1.5 px-2.5 py-1 rounded-full text-[10px] mono font-bold border ${
            mode === 'SIMULATION'
              ? 'bg-amber-500/10 border-amber-500/40 text-amber-300'
              : 'bg-cyan-500/10 border-cyan-500/40 text-cyan-300'
          }`}
        >
          <span className={`w-1.5 h-1.5 rounded-full ${mode === 'SIMULATION' ? 'bg-amber-400 animate-pulse' : 'bg-cyan-400 animate-ping'}`} />
          <span>{mode === 'SIMULATION' ? 'SIMULATION MODE' : 'LIVE HARDWARE'}</span>
        </div>

        {/* Telemetry Connection Status */}
        <div className="hidden lg:flex items-center space-x-1.5 px-2.5 py-1 rounded-lg bg-slate-900/50 border border-slate-800/60 mono text-[11px]">
          <span className={`w-2 h-2 rounded-full ${connected ? 'bg-emerald-400' : 'bg-red-400'}`} />
          <span className={connected ? 'text-emerald-400' : 'text-red-400'}>
            {connected ? 'LINK OK' : 'NO CARRIER'}
          </span>
        </div>

        {/* Synchronized Clocks */}
        <div className="hidden md:flex items-center space-x-2 px-2.5 py-1 rounded-lg bg-slate-900/50 border border-slate-800/60 mono text-xs text-slate-300">
          <span className="text-[#00D1FF] font-medium">{utcTime || '00:00:00 UTC'}</span>
          <span className="text-slate-600">|</span>
          <span className="text-slate-400 text-[11px]">LOC {locTime || '00:00:00'}</span>
        </div>

        {/* Action Controls for Simulation */}
        {mode === 'SIMULATION' && (
          <div className="flex items-center space-x-1.5">
            <button
              onClick={onToggleSim}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center space-x-1.5 shadow-sm active:scale-95 ${
                isRunning && !isPaused
                  ? 'bg-amber-500 hover:bg-amber-400 text-slate-950 shadow-[0_0_12px_rgba(245,158,11,0.25)]'
                  : 'bg-[#00D1FF] hover:bg-[#38E1FF] text-slate-950 shadow-[0_0_15px_rgba(0,209,255,0.35)]'
              }`}
            >
              {isRunning && !isPaused ? (
                <>
                  <Pause className="w-3.5 h-3.5 fill-current" />
                  <span>PAUSE</span>
                </>
              ) : (
                <>
                  <Play className="w-3.5 h-3.5 fill-current" />
                  <span>{isRunning ? 'RESUME' : 'LAUNCH SIM'}</span>
                </>
              )}
            </button>
            <button
              onClick={onResetSim}
              className="p-1.5 px-2 rounded-lg bg-slate-900/60 hover:bg-slate-800 border border-slate-800 hover:border-slate-700 text-slate-400 hover:text-slate-200 text-xs font-medium transition active:scale-95 flex items-center space-x-1"
              title="Reset Simulation"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">RESET</span>
            </button>
          </div>
        )}
      </div>
    </header>
  );
};
