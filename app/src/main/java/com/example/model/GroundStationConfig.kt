package com.example.model

enum class OperatingMode(val label: String) {
    SIMULATION("SIMULATION MODE"),
    LIVE_HARDWARE("LIVE HARDWARE MODE")
}

enum class HardwareConnectionProtocol(val label: String) {
    WEBSOCKET("WebSocket (ESP32 Station / AP)"),
    REST_API("HTTP REST Polling"),
    USB_SERIAL_BRIDGE("USB-Serial / TCP Bridge")
}

data class GroundStationConfig(
    val operatingMode: OperatingMode = OperatingMode.SIMULATION,
    val stationCallsign: String = "VU2-ADV-AETHER",
    val groundStationLat: Double = 13.0827,
    val groundStationLon: Double = 80.2707,
    val groundStationElevationM: Double = 12.0,
    val targetDeploymentAltitudeM: Double = 500.0,
    val landingThresholdAltitudeM: Double = 1.5,
    val telemetryRateHz: Int = 2,
    val protocol: HardwareConnectionProtocol = HardwareConnectionProtocol.WEBSOCKET,
    val serverEndpoint: String = "ws://192.168.4.1:81/telemetry",
    val loraFrequencyMhz: Double = 433.0,
    val loraBandwidthKhz: Int = 125,
    val loraSpreadingFactor: Int = 7,
    val loraCodingRate: String = "4/5",
    val simulatedPacketLossRate: Float = 0.02f, // 2% realistic RF packet loss
    val pressureOffsetHpa: Double = 0.0,
    val tempOffsetC: Double = 0.0,
    val soundAlertsEnabled: Boolean = true,
    val imperialUnits: Boolean = false
)
