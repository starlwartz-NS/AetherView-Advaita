export type MissionPhase = 
  | 'STANDBY'
  | 'ARMED'
  | 'DEPLOYED'
  | 'DESCENT'
  | 'LANDING'
  | 'RECOVERY';

export type OperatingMode = 'SIMULATION' | 'LIVE_HARDWARE';

export type GpsFixStatus = '3D_FIX' | '2D_FIX' | 'NO_FIX' | 'UNAVAILABLE';

export type HealthStatus = 'HEALTHY' | 'WARNING' | 'CRITICAL' | 'OFFLINE';

export interface SubsystemDiagnostic {
  name: string;
  hardware: string;
  status: HealthStatus;
  detail: string;
  lastCheck: string;
}

export interface PhaseTimestamp {
  phase: MissionPhase;
  met: number;
  timeStr: string;
  utcTime: string;
  alt: number;
}

export interface TelemetryData {
  mission_id: string;
  sequence_number: number;
  timestamp_utc: string;
  met: number;
  flight_phase: MissionPhase;
  altitude_m: number;
  vertical_velocity_ms: number;
  temperature_c: number;
  pressure_hpa: number;
  humidity_pct: number;
  aqi: number;
  pm25: number | null;
  pm10: number | null;
  battery_voltage_v: number;
  latitude: number | null;
  longitude: number | null;
  gps_fix: GpsFixStatus;
  sats_visible: number;
  hdop: number | null;
  rssi_dbm: number;
  snr_db: number;
  packets_received: number;
  packets_lost: number;
  packets_duplicate: number;
  prr_pct: number;
  last_packet_time: string;
  apogee_m: number;
  max_descent_vel_ms: number;
  predicted_lat: number | null;
  predicted_lon: number | null;
  landing_lat: number | null;
  landing_lon: number | null;
  recovery_beacon_active: boolean;
}

export interface TelemetryPoint {
  time: string;
  met: number;
  packetId: number;
  phase: MissionPhase;
  alt: number;
  vel: number;
  temp: number;
  pressure: number;
  humidity: number;
  aqi: number;
  pm25: number | null;
  pm10: number | null;
  batt: number;
  rssi: number;
  snr: number;
  lat: number | null;
  lon: number | null;
}

export type EventSeverity = 'INFO' | 'SUCCESS' | 'WARNING' | 'CRITICAL';

export interface MissionEvent {
  id: string;
  time: string;
  utcTime: string;
  met: number;
  phase: MissionPhase;
  severity: EventSeverity;
  type: string;
  msg: string;
  alt?: number;
  coords?: string;
  packetId?: number;
}

export type TabId = 
  | 'overview'
  | 'telemetry'
  | 'analytics'
  | 'tracking'
  | 'comm'
  | 'logs'
  | 'health'
  | 'demo'
  | 'settings';

export interface RadioConfig {
  frequencyMhz: number;
  bandwidthKhz: number;
  spreadingFactor: number;
  codingRate: string;
  txPowerDbm: number;
  callsign: string;
}

export interface HardwareConnectionConfig {
  mode: OperatingMode;
  adapterType: 'WEBSOCKET' | 'HTTP_POLL' | 'SERIAL_BRIDGE';
  endpointUrl: string;
  baudRate: number;
  pollIntervalMs: number;
  radio: RadioConfig;
}

export interface JudgeMissionObjective {
  id: string;
  title: string;
  stage: string;
  description: string;
  targetMetric: string;
  achievedMetric: string | null;
  verified: boolean;
  timestamp: string | null;
}
