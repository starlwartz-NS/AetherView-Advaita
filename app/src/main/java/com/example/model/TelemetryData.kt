package com.example.model

enum class MissionPhase(val displayName: String, val stepNumber: Int) {
    STANDBY("STANDBY", 1),
    ARMED("ARMED", 2),
    DEPLOYED("DEPLOYED", 3),
    DESCENT("DESCENT", 4),
    LANDING_DETECTED("LANDING DETECTED", 5),
    RECOVERY("RECOVERY", 6)
}

enum class HardwareStatus(val label: String) {
    ONLINE("ONLINE"),
    STANDBY("STANDBY"),
    OFFLINE("OFFLINE"),
    SIMULATED("SIMULATED"),
    DATA_STALE("DATA STALE"),
    ERROR("ERROR")
}

enum class BatteryCondition(val label: String) {
    OPTIMAL("OPTIMAL"),
    NOMINAL("NOMINAL"),
    LOW("LOW"),
    CRITICAL("CRITICAL")
}

data class GpsData(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude_m: Double? = null,
    val satellites: Int = 0,
    val hdop: Float? = null,
    val valid: Boolean = false
)

data class CommData(
    val rssi: Int? = null,
    val snr: Float? = null,
    val packet_valid: Boolean = false,
    val crc_ok: Boolean = false,
    val frequency_mhz: Double = 433.000,
    val spreading_factor: Int = 7,
    val bandwidth_khz: Int = 125,
    val coding_rate: String = "4/5"
)

data class TelemetryPacket(
    val device_id: String = "AETHER-CAN-01",
    val packet_id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val iso_timestamp: String = "",
    val mission_phase: MissionPhase = MissionPhase.STANDBY,
    val altitude_m: Double? = null,
    val temperature_c: Double? = null,
    val pressure_hpa: Double? = null,
    val humidity_percent: Double? = null,
    val aqi: Int? = null,
    val descent_velocity_ms: Double? = null,
    val battery_voltage: Double? = null,
    val gps: GpsData = GpsData(),
    val communication: CommData = CommData(),
    val raw_hex_payload: String = "",
    val is_simulated: Boolean = true
)

data class SystemHealth(
    val esp32_status: HardwareStatus = HardwareStatus.STANDBY,
    val lora_status: HardwareStatus = HardwareStatus.STANDBY,
    val bmp280_available: Boolean = true,
    val bme680_available: Boolean = true,
    val mpu6050_available: Boolean = true,
    val pms5003_available: Boolean = true,
    val gps_lock: Boolean = false,
    val battery_condition: BatteryCondition = BatteryCondition.OPTIMAL,
    val is_data_stale: Boolean = false,
    val last_packet_time_ms: Long = 0
)

data class FlightAnalytics(
    val flight_duration_sec: Long = 0,
    val max_altitude_m: Double = 0.0,
    val min_altitude_m: Double = 0.0,
    val avg_descent_velocity_ms: Double = 0.0,
    val terminal_velocity_ms: Double = 0.0,
    val deployment_timestamp: Long? = null,
    val landing_timestamp: Long? = null,
    val total_packets_received: Long = 0,
    val total_packets_lost: Long = 0,
    val packet_reception_rate: Double = 100.0,
    val current_descent_rate_ms: Double = 0.0
)

data class MissionEvent(
    val timestamp: Long,
    val phase: MissionPhase,
    val message: String,
    val altitude_m: Double?
)
