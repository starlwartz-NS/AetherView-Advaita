import { TelemetryData } from '../types/telemetry';

export type TelemetryCallback = (data: TelemetryData) => void;
export type ErrorCallback = (error: string) => void;
export type StatusCallback = (connected: boolean, statusText: string) => void;

export interface IHardwareAdapter {
  connect(): Promise<boolean>;
  disconnect(): void;
  isConnected(): boolean;
  onTelemetry(callback: TelemetryCallback): void;
  onError(callback: ErrorCallback): void;
  onStatusChange(callback: StatusCallback): void;
}
