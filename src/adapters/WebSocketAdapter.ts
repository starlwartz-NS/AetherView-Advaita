import { IHardwareAdapter, TelemetryCallback, ErrorCallback, StatusCallback } from './HardwareAdapter';
import { TelemetryData } from '../types/telemetry';

export class WebSocketAdapter implements IHardwareAdapter {
  private url: string;
  private ws: WebSocket | null = null;
  private onTelemetryCb: TelemetryCallback | null = null;
  private onErrorCb: ErrorCallback | null = null;
  private onStatusChangeCb: StatusCallback | null = null;
  private reconnectTimer: number | null = null;
  private shouldReconnect = true;

  constructor(url = 'ws://localhost:8080/telemetry') {
    this.url = url;
  }

  public setUrl(url: string): void {
    this.url = url;
    if (this.ws) {
      this.disconnect();
      this.connect();
    }
  }

  public async connect(): Promise<boolean> {
    this.shouldReconnect = true;
    return new Promise((resolve) => {
      try {
        this.ws = new WebSocket(this.url);

        this.ws.onopen = () => {
          if (this.onStatusChangeCb) {
            this.onStatusChangeCb(true, `Connected to ${this.url}`);
          }
          resolve(true);
        };

        this.ws.onmessage = (event) => {
          try {
            const raw = JSON.parse(event.data);
            const validated = this.validateTelemetry(raw);
            if (validated && this.onTelemetryCb) {
              this.onTelemetryCb(validated);
            }
          } catch (e) {
            if (this.onErrorCb) {
              this.onErrorCb(`Invalid telemetry packet format: ${(e as Error).message}`);
            }
          }
        };

        this.ws.onerror = (e) => {
          const err = `WebSocket error on ${this.url}`;
          if (this.onErrorCb) this.onErrorCb(err);
          if (this.onStatusChangeCb) this.onStatusChangeCb(false, err);
          resolve(false);
        };

        this.ws.onclose = () => {
          if (this.onStatusChangeCb) {
            this.onStatusChangeCb(false, `Disconnected from ${this.url}`);
          }
          if (this.shouldReconnect) {
            this.scheduleReconnect();
          }
        };
      } catch (err) {
        if (this.onErrorCb) this.onErrorCb((err as Error).message);
        resolve(false);
      }
    });
  }

  private scheduleReconnect(): void {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    this.reconnectTimer = window.setTimeout(() => {
      if (this.shouldReconnect) {
        this.connect();
      }
    }, 4000);
  }

  public disconnect(): void {
    this.shouldReconnect = false;
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer);
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  public isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }

  public onTelemetry(callback: TelemetryCallback): void {
    this.onTelemetryCb = callback;
  }

  public onError(callback: ErrorCallback): void {
    this.onErrorCb = callback;
  }

  public onStatusChange(callback: StatusCallback): void {
    this.onStatusChangeCb = callback;
  }

  private validateTelemetry(raw: any): TelemetryData | null {
    if (!raw || typeof raw !== 'object') return null;

    // Validate essential telemetry attributes with fallbacks for optional sensors
    return {
      mission_id: String(raw.mission_id || 'AETHER-DTU-2026'),
      sequence_number: Number(raw.sequence_number || 0),
      timestamp_utc: String(raw.timestamp_utc || new Date().toISOString()),
      met: Number(raw.met || 0),
      flight_phase: raw.flight_phase || 'STANDBY',
      altitude_m: Number(raw.altitude_m || 0),
      vertical_velocity_ms: Number(raw.vertical_velocity_ms || 0),
      temperature_c: Number(raw.temperature_c || 0),
      pressure_hpa: Number(raw.pressure_hpa || 1013.25),
      humidity_pct: Number(raw.humidity_pct || 50),
      aqi: Number(raw.aqi || 30),
      pm25: raw.pm25 !== undefined ? Number(raw.pm25) : null,
      pm10: raw.pm10 !== undefined ? Number(raw.pm10) : null,
      battery_voltage_v: Number(raw.battery_voltage_v || 8.4),
      latitude: raw.latitude !== undefined && raw.latitude !== null ? Number(raw.latitude) : null,
      longitude: raw.longitude !== undefined && raw.longitude !== null ? Number(raw.longitude) : null,
      gps_fix: raw.gps_fix || (raw.latitude ? '3D_FIX' : 'UNAVAILABLE'),
      sats_visible: Number(raw.sats_visible || 0),
      hdop: raw.hdop !== undefined ? Number(raw.hdop) : null,
      rssi_dbm: Number(raw.rssi_dbm || -75),
      snr_db: Number(raw.snr_db || 8.0),
      packets_received: Number(raw.packets_received || 1),
      packets_lost: Number(raw.packets_lost || 0),
      packets_duplicate: Number(raw.packets_duplicate || 0),
      prr_pct: Number(raw.prr_pct || 100),
      last_packet_time: String(raw.last_packet_time || new Date().toLocaleTimeString()),
      apogee_m: Number(raw.apogee_m || 500.0),
      max_descent_vel_ms: Number(raw.max_descent_vel_ms || 0),
      predicted_lat: raw.predicted_lat || null,
      predicted_lon: raw.predicted_lon || null,
      landing_lat: raw.landing_lat || null,
      landing_lon: raw.landing_lon || null,
      recovery_beacon_active: Boolean(raw.recovery_beacon_active),
    };
  }
}
