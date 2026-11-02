import React, { useState, useEffect, useRef } from 'react';
import { Sidebar } from './components/Sidebar';
import { Header } from './components/Header';
import { OverviewTab } from './components/OverviewTab';
import { TelemetryTab } from './components/TelemetryTab';
import { AnalyticsTab } from './components/AnalyticsTab';
import { TrackingTab } from './components/TrackingTab';
import { CommTab } from './components/CommTab';
import { LogsTab } from './components/LogsTab';
import { SystemHealthPanel } from './components/SystemHealthPanel';
import { JudgeDemoTab } from './components/JudgeDemoTab';
import { SettingsTab } from './components/SettingsTab';
import { 
  TabId, 
  TelemetryData, 
  TelemetryPoint, 
  MissionEvent, 
  PhaseTimestamp, 
  MissionPhase, 
  HardwareConnectionConfig, 
  OperatingMode 
} from './types/telemetry';
import { SimulationEngine } from './adapters/SimulationEngine';
import { WebSocketAdapter } from './adapters/WebSocketAdapter';
import { HttpPollingAdapter } from './adapters/HttpPollingAdapter';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<TabId>('overview');
  const [mode, setMode] = useState<OperatingMode>('SIMULATION');
  const [isRunning, setIsRunning] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [met, setMet] = useState(0);
  const [lastPacketEpoch, setLastPacketEpoch] = useState<number>(Date.now());
  const [staleSeconds, setStaleSeconds] = useState<number>(0);

  const [connectionConfig, setConnectionConfig] = useState<HardwareConnectionConfig>({
    mode: 'SIMULATION',
    adapterType: 'WEBSOCKET',
    endpointUrl: 'ws://localhost:8080/telemetry',
    baudRate: 115200,
    pollIntervalMs: 1000,
    radio: {
      frequencyMhz: 433.0,
      bandwidthKhz: 125,
      spreadingFactor: 7,
      codingRate: '4/5',
      txPowerDbm: 20,
      callsign: 'VU2-ADV-AETHER',
    },
  });

  const [connected, setConnected] = useState(true);
  const [statusText, setStatusText] = useState('SIMULATION READY');

  const [telemetry, setTelemetry] = useState<TelemetryData>({
    mission_id: 'AETHER-DTU-2026',
    sequence_number: 0,
    timestamp_utc: new Date().toUTCString().split(' ')[4] + ' UTC',
    met: 0.0,
    flight_phase: 'STANDBY',
    altitude_m: 0.0,
    vertical_velocity_ms: 0.0,
    temperature_c: 28.5,
    pressure_hpa: 1013.25,
    humidity_pct: 54.0,
    aqi: 32,
    pm25: 18.4,
    pm10: 36.2,
    battery_voltage_v: 8.38,
    latitude: 28.7499,
    longitude: 77.1170,
    gps_fix: '3D_FIX',
    sats_visible: 11,
    hdop: 0.85,
    rssi_dbm: -62,
    snr_db: 9.8,
    packets_received: 0,
    packets_lost: 0,
    packets_duplicate: 0,
    prr_pct: 100.0,
    last_packet_time: '00:00:00 UTC',
    apogee_m: 500.0,
    max_descent_vel_ms: 0.0,
    predicted_lat: 28.7512,
    predicted_lon: 77.1189,
    landing_lat: null,
    landing_lon: null,
    recovery_beacon_active: false,
  });

  const [history, setHistory] = useState<TelemetryPoint[]>([]);

  // Phase transition timestamps record
  const [phaseTimestamps, setPhaseTimestamps] = useState<Record<MissionPhase, PhaseTimestamp | null>>({
    STANDBY: { phase: 'STANDBY', met: 0, timeStr: 'T+00:00.0', utcTime: new Date().toUTCString().split(' ')[4] + ' UTC', alt: 0 },
    ARMED: null,
    DEPLOYED: null,
    DESCENT: null,
    LANDING: null,
    RECOVERY: null,
  });

  const [events, setEvents] = useState<MissionEvent[]>([
    {
      id: 'EVT-001',
      time: 'T+00:00.0',
      utcTime: new Date().toUTCString().split(' ')[4] + ' UTC',
      met: 0,
      phase: 'STANDBY',
      severity: 'SUCCESS',
      type: 'INIT',
      msg: 'AetherView Advaita Ground Station initialized. SX1278 LoRa carrier frequency locked on 433.000 MHz.',
      alt: 0.0,
    },
  ]);

  const simEngineRef = useRef<SimulationEngine>(new SimulationEngine(true));
  const simTimerRef = useRef<number | null>(null);
  const currentPhaseRef = useRef<MissionPhase>('STANDBY');

  const wsAdapterRef = useRef<WebSocketAdapter | null>(null);
  const httpAdapterRef = useRef<HttpPollingAdapter | null>(null);

  // Stale Telemetry Timer
  useEffect(() => {
    const timer = setInterval(() => {
      const elapsed = (Date.now() - lastPacketEpoch) / 1000;
      setStaleSeconds(elapsed);
    }, 500);
    return () => clearInterval(timer);
  }, [lastPacketEpoch]);

  // Handle new incoming telemetry from either Simulation or Live Hardware
  const processIncomingTelemetry = (data: TelemetryData) => {
    setLastPacketEpoch(Date.now());
    setMet(data.met);

    // Detect phase change
    if (data.flight_phase !== currentPhaseRef.current) {
      const prevPhase = currentPhaseRef.current;
      currentPhaseRef.current = data.flight_phase;

      const mins = Math.floor(data.met / 60).toString().padStart(2, '0');
      const secs = Math.floor(data.met % 60).toString().padStart(2, '0');
      const tenths = Math.floor((data.met % 1) * 10);
      const timeStr = `T+${mins}:${secs}.${tenths}`;
      const utcTime = data.timestamp_utc;

      setPhaseTimestamps((prev) => ({
        ...prev,
        [data.flight_phase]: {
          phase: data.flight_phase,
          met: data.met,
          timeStr,
          utcTime,
          alt: data.altitude_m,
        },
      }));

      // Generate phase transition audit event
      const eventId = `EVT-${(Date.now() % 10000).toString().padStart(4, '0')}`;
      let phaseMsg = `Mission phase transition: ${prevPhase} ➔ ${data.flight_phase}`;
      let severity: 'INFO' | 'SUCCESS' | 'WARNING' = 'INFO';

      if (data.flight_phase === 'DEPLOYED') {
        phaseMsg = `Deployment confirmed at ${data.altitude_m.toFixed(1)}m apogee! Chute ejection sequence activated.`;
        severity = 'SUCCESS';
      } else if (data.flight_phase === 'DESCENT') {
        phaseMsg = `Parachute canopy inflated. Aerodynamic descent stabilized at ${Math.abs(data.vertical_velocity_ms).toFixed(1)} m/s.`;
        severity = 'SUCCESS';
      } else if (data.flight_phase === 'LANDING') {
        phaseMsg = `Touchdown confirmed! Surface elevation locked. Impact shock absorbed safely.`;
        severity = 'SUCCESS';
      } else if (data.flight_phase === 'RECOVERY') {
        phaseMsg = `Recovery beacon active! Audio buzzer pinging at 2.4 kHz. Coordinates broadcasted.`;
        severity = 'SUCCESS';
      }

      setEvents((prev) => [
        {
          id: eventId,
          time: timeStr,
          utcTime,
          met: data.met,
          phase: data.flight_phase,
          severity,
          type: 'PHASE',
          msg: phaseMsg,
          alt: data.altitude_m,
          packetId: data.sequence_number,
        },
        ...prev.slice(0, 150),
      ]);
    }

    setTelemetry(data);

    // Append to history buffer (cap at 300 points for memory safety)
    const mins = Math.floor(data.met / 60).toString().padStart(2, '0');
    const secs = Math.floor(data.met % 60).toString().padStart(2, '0');
    const timeLabel = `${mins}:${secs}`;

    const newPoint: TelemetryPoint = {
      time: timeLabel,
      met: data.met,
      packetId: data.sequence_number,
      phase: data.flight_phase,
      alt: data.altitude_m,
      vel: data.vertical_velocity_ms,
      temp: data.temperature_c,
      pressure: data.pressure_hpa,
      humidity: data.humidity_pct,
      aqi: data.aqi,
      pm25: data.pm25,
      pm10: data.pm10,
      batt: data.battery_voltage_v,
      rssi: data.rssi_dbm,
      snr: data.snr_db,
      lat: data.latitude,
      lon: data.longitude,
    };

    setHistory((prev) => {
      const updated = [...prev, newPoint];
      return updated.length > 300 ? updated.slice(updated.length - 300) : updated;
    });
  };

  // Simulation Loop
  useEffect(() => {
    if (mode !== 'SIMULATION' || !isRunning || isPaused) {
      if (simTimerRef.current) clearInterval(simTimerRef.current);
      return;
    }

    simTimerRef.current = window.setInterval(() => {
      const nextData = simEngineRef.current.step(0.5);
      processIncomingTelemetry(nextData);
    }, 500);

    return () => {
      if (simTimerRef.current) clearInterval(simTimerRef.current);
    };
  }, [mode, isRunning, isPaused]);

  // Live Hardware Connection Management
  useEffect(() => {
    if (mode === 'LIVE_HARDWARE') {
      if (simTimerRef.current) clearInterval(simTimerRef.current);
      setIsRunning(false);

      if (connectionConfig.adapterType === 'WEBSOCKET') {
        const ws = new WebSocketAdapter(connectionConfig.endpointUrl);
        wsAdapterRef.current = ws;

        ws.onStatusChange((isConn, msg) => {
          setConnected(isConn);
          setStatusText(msg);
        });

        ws.onTelemetry((liveData) => {
          processIncomingTelemetry(liveData);
        });

        ws.onError((err) => {
          setEvents((prev) => [
            {
              id: `ERR-${Date.now() % 10000}`,
              time: 'T+00:00.0',
              utcTime: new Date().toUTCString().split(' ')[4] + ' UTC',
              met,
              phase: telemetry.flight_phase,
              severity: 'WARNING',
              type: 'HARDWARE_LINK',
              msg: err,
            },
            ...prev,
          ]);
        });

        ws.connect();

        return () => {
          ws.disconnect();
          wsAdapterRef.current = null;
        };
      } else if (connectionConfig.adapterType === 'HTTP_POLL') {
        const http = new HttpPollingAdapter(connectionConfig.endpointUrl, connectionConfig.pollIntervalMs);
        httpAdapterRef.current = http;

        http.onStatusChange((isConn, msg) => {
          setConnected(isConn);
          setStatusText(msg);
        });

        http.onTelemetry((liveData) => {
          processIncomingTelemetry(liveData);
        });

        http.connect();

        return () => {
          http.disconnect();
          httpAdapterRef.current = null;
        };
      }
    } else {
      setConnected(true);
      setStatusText('SIMULATION READY');
    }
  }, [mode, connectionConfig.adapterType, connectionConfig.endpointUrl]);

  // Simulation Controls
  const toggleSim = () => {
    if (!isRunning) {
      setIsRunning(true);
      setIsPaused(false);
    } else if (!isPaused) {
      setIsPaused(true);
    } else {
      setIsPaused(false);
    }
  };

  const resetSim = () => {
    setIsRunning(false);
    setIsPaused(false);
    setMet(0);
    currentPhaseRef.current = 'STANDBY';
    simEngineRef.current.reset();

    setPhaseTimestamps({
      STANDBY: { phase: 'STANDBY', met: 0, timeStr: 'T+00:00.0', utcTime: new Date().toUTCString().split(' ')[4] + ' UTC', alt: 0 },
      ARMED: null,
      DEPLOYED: null,
      DESCENT: null,
      LANDING: null,
      RECOVERY: null,
    });

    setTelemetry({
      mission_id: 'AETHER-DTU-2026',
      sequence_number: 0,
      timestamp_utc: new Date().toUTCString().split(' ')[4] + ' UTC',
      met: 0.0,
      flight_phase: 'STANDBY',
      altitude_m: 0.0,
      vertical_velocity_ms: 0.0,
      temperature_c: 28.5,
      pressure_hpa: 1013.25,
      humidity_pct: 54.0,
      aqi: 32,
      pm25: 18.4,
      pm10: 36.2,
      battery_voltage_v: 8.38,
      latitude: 28.7499,
      longitude: 77.1170,
      gps_fix: '3D_FIX',
      sats_visible: 11,
      hdop: 0.85,
      rssi_dbm: -62,
      snr_db: 9.8,
      packets_received: 0,
      packets_lost: 0,
      packets_duplicate: 0,
      prr_pct: 100.0,
      last_packet_time: '00:00:00 UTC',
      apogee_m: 500.0,
      max_descent_vel_ms: 0.0,
      predicted_lat: 28.7512,
      predicted_lon: 77.1189,
      landing_lat: null,
      landing_lon: null,
      recovery_beacon_active: false,
    });

    setHistory([]);
    setEvents([
      {
        id: `EVT-${Date.now() % 10000}`,
        time: 'T+00:00.0',
        utcTime: new Date().toUTCString().split(' ')[4] + ' UTC',
        met: 0,
        phase: 'STANDBY',
        severity: 'INFO',
        type: 'RESET',
        msg: 'Mission reset executed. Flight computer and ground station state initialized.',
      },
    ]);
  };

  const handleUpdateConfig = (newConfig: HardwareConnectionConfig) => {
    setConnectionConfig(newConfig);
    setMode(newConfig.mode);
  };

  return (
    <div className="flex w-screen h-screen bg-transparent text-[#F2F6FC] overflow-hidden select-none">
      {/* 1. FIXED 240px LEFT DESKTOP SIDEBAR */}
      <Sidebar
        currentTab={currentTab}
        onSelectTab={(tab) => setCurrentTab(tab)}
        mode={mode}
        connected={connected}
        statusText={statusText}
      />

      {/* 2. MAIN WORKSTATION DASHBOARD (100% REMAINING WIDTH) */}
      <main className="flex-1 flex flex-col h-full overflow-hidden min-w-0">
        <Header
          telemetry={telemetry}
          met={met}
          mode={mode}
          connected={connected}
          isRunning={isRunning}
          isPaused={isPaused}
          onToggleSim={toggleSim}
          onResetSim={resetSim}
        />

        {/* Scrollable Multi-Column Content Area */}
        <div className="flex-1 p-4.5 overflow-y-auto custom-scroll">
          {currentTab === 'overview' && (
            <OverviewTab
              telemetry={telemetry}
              history={history}
              events={events}
              phaseTimestamps={phaseTimestamps}
            />
          )}

          {currentTab === 'telemetry' && (
            <TelemetryTab
              telemetry={telemetry}
              history={history}
              staleSeconds={staleSeconds}
            />
          )}

          {currentTab === 'analytics' && (
            <AnalyticsTab
              telemetry={telemetry}
              history={history}
              met={met}
              mode={mode}
            />
          )}

          {currentTab === 'tracking' && (
            <TrackingTab telemetry={telemetry} />
          )}

          {currentTab === 'comm' && (
            <CommTab
              telemetry={telemetry}
              radioConfig={connectionConfig.radio}
              connected={connected}
            />
          )}

          {currentTab === 'logs' && (
            <LogsTab events={events} />
          )}

          {currentTab === 'health' && (
            <SystemHealthPanel
              telemetry={telemetry}
              mode={mode}
              isStale={staleSeconds >= 3.0}
              staleSeconds={staleSeconds}
            />
          )}

          {currentTab === 'demo' && (
            <JudgeDemoTab
              telemetry={telemetry}
              met={met}
              isRunning={isRunning}
              isPaused={isPaused}
              onStartDemo={toggleSim}
              onPauseDemo={toggleSim}
              onResetDemo={resetSim}
            />
          )}

          {currentTab === 'settings' && (
            <SettingsTab
              config={connectionConfig}
              onUpdateConfig={handleUpdateConfig}
              connected={connected}
            />
          )}
        </div>
      </main>
    </div>
  );
};
