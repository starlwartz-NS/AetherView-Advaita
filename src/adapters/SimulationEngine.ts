import { TelemetryData, MissionPhase } from '../types/telemetry';

export class SimulationEngine {
  private met = 0;
  private sequenceNumber = 0;
  private apogeeTarget = 500.0;
  private maxDescentVel = 0;
  private isDeterministic = true;
  private seed = 42;

  // Launch pad reference coordinates (DTU Delhi / RYIC Competition site)
  private baseLat = 28.7499;
  private baseLon = 77.1170;
  private targetLandingLat = 28.7512;
  private targetLandingLon = 77.1189;

  constructor(deterministic = true) {
    this.isDeterministic = deterministic;
  }

  public reset(): void {
    this.met = 0;
    this.sequenceNumber = 0;
    this.maxDescentVel = 0;
    this.seed = 42;
  }

  private pseudoRandom(): number {
    if (!this.isDeterministic) {
      return Math.random();
    }
    // High-quality deterministic pseudo-random sequence (LCCG)
    this.seed = (this.seed * 1664525 + 1013904223) % 4294967296;
    return this.seed / 4294967296;
  }

  public step(dt = 0.5): TelemetryData {
    this.met += dt;
    this.sequenceNumber += 1;

    let phase: MissionPhase = 'STANDBY';
    let alt = 0.0;
    let vel = 0.0;
    let recoveryBeacon = false;

    // Phase transition schedule
    if (this.met < 4.0) {
      phase = 'STANDBY';
      alt = 0.0;
      vel = 0.0;
    } else if (this.met < 8.0) {
      phase = 'ARMED';
      alt = 0.0;
      vel = 0.0;
    } else if (this.met < 11.0) {
      phase = 'DEPLOYED';
      alt = this.apogeeTarget;
      vel = -2.1;
    } else {
      // Descent Phase
      const descentElapsed = this.met - 11.0;
      // Parachute terminal velocity equilibrium: 6.4 m/s with slight aerodynamic gust
      const gust = (this.pseudoRandom() - 0.5) * 0.35;
      const targetTerminalVel = -(6.4 + gust);

      const calculatedAlt = Math.max(0.0, this.apogeeTarget - descentElapsed * 6.45);

      if (calculatedAlt > 1.0) {
        phase = 'DESCENT';
        alt = calculatedAlt;
        vel = targetTerminalVel;
        if (Math.abs(vel) > this.maxDescentVel) {
          this.maxDescentVel = Math.abs(vel);
        }
      } else {
        // Touchdown
        alt = 0.2;
        vel = 0.0;
        if (this.met < 95.0) {
          phase = 'LANDING';
        } else {
          phase = 'RECOVERY';
          recoveryBeacon = true;
        }
      }
    }

    // Standard International Atmosphere (ISA) Model
    // T = T0 - (0.0065 °C/m) * h
    const baseTemp = 28.5;
    const temp = baseTemp - 0.0065 * alt + (this.pseudoRandom() - 0.5) * 0.15;

    // Barometric formula: P = P0 * exp(-M*g*h / (R*T))
    const p0 = 1013.25;
    const molarMass = 0.0289644; // kg/mol
    const g = 9.80665; // m/s^2
    const rGas = 8.3144598; // J/(mol·K)
    const tKelvin = 273.15 + temp;
    const pressure = p0 * Math.exp((-molarMass * g * alt) / (rGas * tKelvin));

    // Humidity profile
    const humidity = Math.min(95, Math.max(35, 54.0 + (alt / 500.0) * 8.5 + (this.pseudoRandom() - 0.5) * 1.0));

    // Laser Air Quality Index (PMS5003 simulation: particulate increases near surface)
    const altitudeFactor = 1.0 - (alt / 500.0) * 0.45;
    const pm25 = Math.max(8.0, Number((24.0 * altitudeFactor + (this.pseudoRandom() - 0.5) * 2.0).toFixed(1)));
    const pm10 = Math.max(15.0, Number((42.0 * altitudeFactor + (this.pseudoRandom() - 0.5) * 3.5).toFixed(1)));
    const aqi = Math.max(25, Math.round(pm25 * 1.85));

    // LiPo 2S Cell Discharge curve (8.4V max, drops microvolts per transmission)
    const batt = Math.max(7.85, Number((8.38 - (this.met / 600.0) * 0.15).toFixed(2)));

    // GPS Geodetic Drift Model
    const progress = Math.min(1.0, Math.max(0.0, 1.0 - alt / this.apogeeTarget));
    const lat = this.baseLat + (this.targetLandingLat - this.baseLat) * progress + (this.pseudoRandom() - 0.5) * 0.00003;
    const lon = this.baseLon + (this.targetLandingLon - this.baseLon) * progress + (this.pseudoRandom() - 0.5) * 0.00003;

    // RF LoRa Path Loss
    const slantRangeMeters = Math.sqrt(alt * alt + 2500);
    const pathLoss = 20 * Math.log10(Math.max(10, slantRangeMeters) / 10);
    const rssi = Math.min(-42, Math.max(-118, Math.round(-46 - pathLoss)));
    const snr = Number((9.8 - (slantRangeMeters / 600) * 2.5 + (this.pseudoRandom() - 0.5) * 0.4).toFixed(1));

    const packetsReceived = this.sequenceNumber;
    const packetsLost = Math.floor(this.sequenceNumber * 0.006);
    const prr = Number((((packetsReceived - packetsLost) / packetsReceived) * 100).toFixed(1));

    const now = new Date();
    const timestampUtc = now.toISOString().split('T')[1].slice(0, 8) + ' UTC';

    return {
      mission_id: 'AETHER-DTU-2026',
      sequence_number: this.sequenceNumber,
      timestamp_utc: timestampUtc,
      met: Number(this.met.toFixed(1)),
      flight_phase: phase,
      altitude_m: Number(alt.toFixed(1)),
      vertical_velocity_ms: Number(vel.toFixed(2)),
      temperature_c: Number(temp.toFixed(1)),
      pressure_hpa: Number(pressure.toFixed(1)),
      humidity_pct: Number(humidity.toFixed(1)),
      aqi,
      pm25,
      pm10,
      battery_voltage_v: batt,
      latitude: Number(lat.toFixed(6)),
      longitude: Number(lon.toFixed(6)),
      gps_fix: '3D_FIX',
      sats_visible: 11,
      hdop: 0.85,
      rssi_dbm: rssi,
      snr_db: snr,
      packets_received: packetsReceived,
      packets_lost: packetsLost,
      packets_duplicate: 0,
      prr_pct: prr,
      last_packet_time: timestampUtc,
      apogee_m: this.apogeeTarget,
      max_descent_vel_ms: Number(this.maxDescentVel.toFixed(2)),
      predicted_lat: this.targetLandingLat,
      predicted_lon: this.targetLandingLon,
      landing_lat: phase === 'LANDING' || phase === 'RECOVERY' ? this.targetLandingLat : null,
      landing_lon: phase === 'LANDING' || phase === 'RECOVERY' ? this.targetLandingLon : null,
      recovery_beacon_active: recoveryBeacon,
    };
  }
}
