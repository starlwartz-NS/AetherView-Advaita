import React from 'react';
import { 
  LayoutDashboard, 
  Activity, 
  BarChart3, 
  Compass, 
  Radio, 
  FileText, 
  Sliders,
  ShieldCheck,
  Award,
  Wifi,
  Sparkles
} from 'lucide-react';
import { TabId, OperatingMode } from '../types/telemetry';

interface SidebarProps {
  currentTab: TabId;
  onSelectTab: (tab: TabId) => void;
  mode: OperatingMode;
  connected: boolean;
  statusText: string;
}

export const Sidebar: React.FC<SidebarProps> = ({ 
  currentTab, 
  onSelectTab, 
  mode,
  connected,
  statusText 
}) => {
  const navItems: { id: TabId; label: string; icon: React.ReactNode; badge?: string }[] = [
    { id: 'overview', label: 'Mission Overview', icon: <LayoutDashboard className="w-4 h-4" /> },
    { id: 'telemetry', label: 'Live Telemetry', icon: <Activity className="w-4 h-4" /> },
    { id: 'analytics', label: 'Flight Analytics', icon: <BarChart3 className="w-4 h-4" /> },
    { id: 'tracking', label: 'GPS Tracking & Recovery', icon: <Compass className="w-4 h-4" /> },
    { id: 'comm', label: 'LoRa Comms Monitor', icon: <Radio className="w-4 h-4" /> },
    { id: 'logs', label: 'Mission Logbook', icon: <FileText className="w-4 h-4" /> },
    { id: 'health', label: 'System Diagnostics', icon: <ShieldCheck className="w-4 h-4" /> },
    { id: 'demo', label: 'Judge Demo Mode', icon: <Award className="w-4 h-4" />, badge: 'RYIC 2026' },
    { id: 'settings', label: 'Hardware & Uplink', icon: <Sliders className="w-4 h-4" /> },
  ];

  return (
    <aside 
      style={{ width: '240px', minWidth: '240px' }} 
      className="flex flex-col justify-between p-3.5 h-full bg-[#050811]/95 backdrop-blur-md border-r border-slate-800/80 shrink-0 select-none z-20"
    >
      <div>
        {/* Brand Header */}
        <div className="flex items-center space-x-3 pb-3.5 border-b border-slate-800/80">
          <img
            src="/aetherview-advaita-logo.png"
            alt="AetherView Advaita logo"
            className="w-10 h-10 rounded-full object-cover shrink-0"
          />
          <div className="min-w-0">
            <div className="font-extrabold text-white text-sm tracking-wider leading-tight flex items-center space-x-1.5">
              <span>AetherView Advaita</span>
            </div>
            <div className="text-[10px] text-cyan-400 font-semibold tracking-wide">
              GROUND STATION
            </div>
            <div className="text-[9px] text-slate-400 truncate">
              Advaita Mission Control
            </div>
          </div>
        </div>

        {/* Operating Mode Indicator Tag */}
        <div className="mt-3 px-2">
          <div
            className={`flex items-center justify-between px-2.5 py-1.5 rounded-lg border text-[10px] mono font-bold ${
              mode === 'SIMULATION'
                ? 'bg-amber-500/10 border-amber-500/30 text-amber-300'
                : 'bg-cyan-500/10 border-cyan-500/30 text-cyan-300'
            }`}
          >
            <span>MODE:</span>
            <span className="flex items-center space-x-1">
              <span className={`w-1.5 h-1.5 rounded-full ${mode === 'SIMULATION' ? 'bg-amber-400 animate-pulse' : 'bg-cyan-400 animate-ping'}`} />
              <span>{mode}</span>
            </span>
          </div>
        </div>

        {/* Navigation Modules */}
        <div className="mt-3.5">
          <div className="text-[10px] font-semibold text-slate-500 tracking-wider uppercase mb-1.5 px-2.5">
            Command Center
          </div>
          <nav className="space-y-1">
            {navItems.map((item) => {
              const isActive = currentTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => onSelectTab(item.id)}
                  className={`w-full flex items-center justify-between px-3 py-2 rounded-xl text-xs font-medium transition-all relative ${
                    isActive
                      ? 'bg-cyan-500/10 text-white border border-cyan-500/35 shadow-[0_0_15px_-3px_rgba(0,209,255,0.2)]'
                      : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/40 border border-transparent'
                  }`}
                >
                  <div className="flex items-center space-x-2.5 min-w-0">
                    {isActive && (
                      <span className="absolute left-0 top-2 bottom-2 w-1 rounded-r bg-[#00D1FF] shadow-[0_0_8px_#00D1FF]" />
                    )}
                    <span className={isActive ? 'text-[#00D1FF]' : 'text-slate-500'}>
                      {item.icon}
                    </span>
                    <span className="truncate">{item.label}</span>
                  </div>

                  {item.badge && (
                    <span className="text-[9px] mono px-1.5 py-0.2 rounded bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-bold shrink-0">
                      {item.badge}
                    </span>
                  )}
                </button>
              );
            })}
          </nav>
        </div>
      </div>

      {/* Station Hardware & Link Badge */}
      <div className="glass-panel p-3 rounded-xl border border-slate-800/80 bg-slate-900/50">
        <div className="flex items-center justify-between mb-1.5">
          <div className="flex items-center space-x-2">
            <span className="relative flex h-2 w-2">
              <span className={`animate-ping absolute inline-flex h-full w-full rounded-full ${connected ? 'bg-emerald-400' : 'bg-red-400'} opacity-75`}></span>
              <span className={`relative inline-flex rounded-full h-2 w-2 ${connected ? 'bg-emerald-500' : 'bg-red-500'}`}></span>
            </span>
            <span className={`text-[10px] font-bold mono ${connected ? 'text-emerald-400' : 'text-red-400'}`}>
              {connected ? 'LINK LOCKED' : 'DISCONNECTED'}
            </span>
          </div>
          <Wifi className={`w-3.5 h-3.5 ${connected ? 'text-emerald-400' : 'text-red-400'}`} />
        </div>
        <div className="text-[10px] text-slate-300 mono font-medium truncate">
          {statusText || 'STATION READY'}
        </div>
        <div className="text-[9px] text-slate-400 mono mt-1 flex justify-between">
          <span>433.000 MHz</span>
          <span className="text-cyan-400">SF7 / BW125</span>
        </div>
      </div>
    </aside>
  );
};
