import { IHardwareAdapter, TelemetryCallback, ErrorCallback, StatusCallback } from './HardwareAdapter';
import { TelemetryData } from '../types/telemetry';

export class HttpPollingAdapter implements IHardwareAdapter {
  private url: string;
  private pollIntervalMs: number;
  private timer: number | null = null;
  private connected = false;
  private onTelemetryCb: TelemetryCallback | null = null;
  private onErrorCb: ErrorCallback | null = null;
  private onStatusChangeCb: StatusCallback | null = null;

  constructor(url = 'http://localhost:8080/api/telemetry', pollIntervalMs = 1000) {
    this.url = url;
    this.pollIntervalMs = pollIntervalMs;
  }

  public setConfig(url: string, pollIntervalMs: number): void {
    this.url = url;
    this.pollIntervalMs = pollIntervalMs;
    if (this.connected) {
      this.disconnect();
      this.connect();
    }
  }

  public async connect(): Promise<boolean> {
    this.connected = true;
    if (this.onStatusChangeCb) {
      this.onStatusChangeCb(true, `Polling ${this.url} every ${this.pollIntervalMs}ms`);
    }
    this.startPolling();
    return true;
  }

  private startPolling(): void {
    const poll = async () => {
      if (!this.connected) return;
      try {
        const response = await fetch(this.url, { headers: { Accept: 'application/json' } });
        if (!response.ok) {
          throw new Error(`HTTP status ${response.status}`);
        }
        const data = await response.json();
        if (this.onTelemetryCb && data) {
          this.onTelemetryCb(data);
        }
      } catch (err) {
        if (this.onErrorCb) {
          this.onErrorCb(`REST poll failed: ${(err as Error).message}`);
        }
      } finally {
        if (this.connected) {
          this.timer = window.setTimeout(poll, this.pollIntervalMs);
        }
      }
    };
    poll();
  }

  public disconnect(): void {
    this.connected = false;
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
    if (this.onStatusChangeCb) {
      this.onStatusChangeCb(false, `Stopped polling ${this.url}`);
    }
  }

  public isConnected(): boolean {
    return this.connected;
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
}
