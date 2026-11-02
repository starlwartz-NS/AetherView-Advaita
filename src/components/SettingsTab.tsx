import React, { useState } from 'react';
import { 
  Sliders, 
  Cpu, 
  Globe, 
  Radio, 
  Check, 
  Wifi, 
  Terminal, 
  ShieldAlert,
  Server
} from 'lucide-react';
import { HardwareConnectionConfig, OperatingMode, RadioConfig } from '../types/telemetry';

interface SettingsTabProps {
  config: HardwareConnectionConfig;
  onUpdateConfig: (newConfig: HardwareConnectionConfig) => void;
  connected: boolean;
}

export const SettingsTab: React.FC<SettingsTabProps> = ({ config, onUpdateConfig, connected }) => {
  const [mode, setMode] = useState<OperatingMode>(config.mode);
  const [adapterType, setAdapterType] = useState(config.adapterType);
  const [endpointUrl, setEndpointUrl] = useState(config.endpointUrl);
  const [baudRate, setBaudRate] = useState(config.baudRate);
  const [pollIntervalMs, setPollIntervalMs] = useState(config.pollIntervalMs);

  const [frequencyMhz, setFrequencyMhz] = useState(config.radio.frequencyMhz);
  const [bandwidthKhz, setBandwidthKhz] = useState(config.radio.bandwidthKhz);
  const [spreadingFactor, setSpreadingFactor] = useState(config.radio.spreadingFactor);
  const [codingRate, setCodingRate] = useState(config.radio.codingRate);
  const [callsign, setCallsign] = useState(config.radio.callsign);

  const [saved, setSaved] = useState(false);

  const handleSave = () => {
    const updated: HardwareConnectionConfig = {
      mode,
      adapterType,
      endpointUrl,
      baudRate,
      pollIntervalMs,
      radio: {
        frequencyMhz,
        bandwidthKhz,
        spreadingFactor,
        codingRate,
        txPowerDbm: 20,
        callsign,
      },
    };
    onUpdateConfig(updated);
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  return (
    <div className="space-y-4">
      {/* Operating Mode Selector Banner */}
      <div className="glass-panel p-5 rounded-xl border border-slate-800/80">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 pb-3 border-b border-slate-800/60 mb-4">
          <div>
            <h2 className="text-sm font-bold text-white uppercase tracking-wider">
              Telemetry Ingestion Mode Selection
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Select between the deterministic physics simulation and live physical ground station hardware.
            </p>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={() => setMode('SIMULATION')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${
                mode === 'SIMULATION'
                  ? 'bg-amber-500 text-slate-950 shadow-[0_0_12px_rgba(245,158,11,0.3)]'
                  : 'bg-slate-900 text-slate-400 hover:text-white border border-slate-800'
              }`}
            >
              SIMULATION ENGINE
            </button>
            <button
              onClick={() => setMode('LIVE_HARDWARE')}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all ${
                mode === 'LIVE_HARDWARE'
                  ? 'bg-cyan-500 text-slate-950 shadow-[0_0_12px_rgba(0,209,255,0.35)]'
                  : 'bg-slate-900 text-slate-400 hover:text-white border border-slate-800'
              }`}
            >
              LIVE HARDWARE UPLINK
            </button>
          </div>
        </div>

        {mode === 'LIVE_HARDWARE' && (
          <div className="p-3 rounded-lg bg-cyan-950/20 border border-cyan-500/30 text-xs mono text-cyan-300 flex items-center space-x-2">
            <Radio className="w-4 h-4 text-cyan-400 shrink-0" />
            <span>
              LIVE HARDWARE MODE ACTIVE: Telemetry will only be ingested from physical ESP32 / LoRa adapters. Simulated readings will NOT be fabricated when offline.
            </span>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {/* Hardware Adapter Configuration */}
        <div className="glass-panel p-5 rounded-xl space-y-4">
          <div className="flex items-center space-x-2 pb-2.5 border-b border-slate-800/60">
            <Server className="w-4 h-4 text-cyan-400" />
            <h3 className="text-xs font-bold text-white uppercase tracking-wider">
              Ground Station Hardware Communication Adapter
            </h3>
          </div>

          <div className="space-y-3 text-xs">
            <div>
              <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                Adapter Protocol
              </label>
              <select
                value={adapterType}
                onChange={(e) => setAdapterType(e.target.value as any)}
                className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
              >
                <option value="WEBSOCKET">WebSocket Stream (Low-latency bidirectional)</option>
                <option value="HTTP_POLL">HTTP REST Polling (Periodic telemetry fetch)</option>
                <option value="SERIAL_BRIDGE">Web Serial Bridge (USB COM Port Gateway)</option>
              </select>
            </div>

            <div>
              <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                Adapter Endpoint URL
              </label>
              <input
                type="text"
                value={endpointUrl}
                onChange={(e) => setEndpointUrl(e.target.value)}
                className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-cyan-300 mono focus:outline-none focus:border-cyan-400"
              />
            </div>

            <div className="grid grid-cols-2 gap-2.5">
              <div>
                <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                  Baud Rate (UART)
                </label>
                <select
                  value={baudRate}
                  onChange={(e) => setBaudRate(Number(e.target.value))}
                  className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
                >
                  <option value={115200}>115200 bps (Standard ESP32)</option>
                  <option value={57600}>57600 bps</option>
                  <option value={9600}>9600 bps</option>
                </select>
              </div>

              <div>
                <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                  Poll Interval (ms)
                </label>
                <input
                  type="number"
                  value={pollIntervalMs}
                  onChange={(e) => setPollIntervalMs(Number(e.target.value))}
                  className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
                />
              </div>
            </div>
          </div>
        </div>

        {/* LoRa Transceiver RF Settings */}
        <div className="glass-panel p-5 rounded-xl space-y-4 flex flex-col justify-between">
          <div>
            <div className="flex items-center space-x-2 pb-2.5 border-b border-slate-800/60 mb-3">
              <Radio className="w-4 h-4 text-emerald-400" />
              <h3 className="text-xs font-bold text-white uppercase tracking-wider">
                SX1278 Physical Radio Layer Settings
              </h3>
            </div>

            <div className="space-y-3 text-xs">
              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                    Center Frequency (MHz)
                  </label>
                  <input
                    type="number"
                    step="0.025"
                    value={frequencyMhz}
                    onChange={(e) => setFrequencyMhz(Number(e.target.value))}
                    className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-cyan-300 mono focus:outline-none focus:border-cyan-400"
                  />
                </div>

                <div>
                  <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                    Signal Bandwidth (kHz)
                  </label>
                  <select
                    value={bandwidthKhz}
                    onChange={(e) => setBandwidthKhz(Number(e.target.value))}
                    className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
                  >
                    <option value={125}>125 kHz (Standard)</option>
                    <option value={250}>250 kHz</option>
                    <option value={500}>500 kHz</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2.5">
                <div>
                  <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                    Spreading Factor
                  </label>
                  <select
                    value={spreadingFactor}
                    onChange={(e) => setSpreadingFactor(Number(e.target.value))}
                    className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
                  >
                    <option value={7}>SF7 (Optimal balance)</option>
                    <option value={8}>SF8</option>
                    <option value={9}>SF9</option>
                    <option value={10}>SF10 (Long range)</option>
                  </select>
                </div>

                <div>
                  <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                    Coding Rate
                  </label>
                  <select
                    value={codingRate}
                    onChange={(e) => setCodingRate(e.target.value)}
                    className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-slate-200 mono focus:outline-none focus:border-cyan-400"
                  >
                    <option value="4/5">CR 4/5 (Standard)</option>
                    <option value="4/6">CR 4/6</option>
                    <option value="4/7">CR 4/7</option>
                    <option value="4/8">CR 4/8</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="text-[10px] mono uppercase text-slate-400 block mb-1.5 font-medium">
                  Station Identification Callsign
                </label>
                <input
                  type="text"
                  value={callsign}
                  onChange={(e) => setCallsign(e.target.value)}
                  className="w-full bg-slate-900/60 p-2.5 rounded-lg border border-slate-800 text-white mono focus:outline-none focus:border-cyan-400"
                />
              </div>
            </div>
          </div>

          <div className="pt-3">
            <button
              onClick={handleSave}
              className={`w-full py-2.5 rounded-lg text-xs font-bold transition-all flex items-center justify-center space-x-2 active:scale-98 ${
                saved
                  ? 'bg-emerald-500 text-slate-950 shadow-[0_0_15px_rgba(16,185,129,0.3)]'
                  : 'bg-cyan-500 hover:bg-cyan-400 text-slate-950 shadow-[0_0_15px_rgba(0,209,255,0.3)]'
              }`}
            >
              {saved ? (
                <>
                  <Check className="w-4 h-4" />
                  <span>CONFIGURATION SAVED</span>
                </>
              ) : (
                <span>SAVE CONFIGURATION</span>
              )}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
