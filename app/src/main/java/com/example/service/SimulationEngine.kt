package com.example.service

import com.example.model.BatteryCondition
import com.example.model.CommData
import com.example.model.GpsData
import com.example.model.HardwareStatus
import com.example.model.MissionEvent
import com.example.model.MissionPhase
import com.example.model.SystemHealth
import com.example.model.TelemetryPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class SimulationEngine(
    private val scope: CoroutineScope
) {
    private var job: Job? = null

    private val _isRunning = MutableStateFlow(false)
    val isRunning = _isRunning.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private val _packetFlow = MutableSharedFlow<TelemetryPacket>(extraBufferCapacity = 64)
    val packetFlow = _packetFlow.asSharedFlow()

    private val _eventFlow = MutableSharedFlow<MissionEvent>(extraBufferCapacity = 32)
    val eventFlow = _eventFlow.asSharedFlow()

    // Simulation Physics State
    private var currentPhase: MissionPhase = MissionPhase.STANDBY
    private var packetCounter: Long = 0
    private var simTimeSeconds: Double = 0.0
    private var altitudeM: Double = 0.0
    private var velocityMs: Double = 0.0
    private var batteryVoltage: Double = 8.38
    private var batteryCapacity: Double = 100.0

    // Ground Station Coordinates (Chennai / Sriharikota region reference)
    private val baseLat = 13.0827
    private val baseLon = 80.2707
    private var currentLat = baseLat
    private var currentLon = baseLon

    // Launch Parameters
    private val deploymentTargetAltitude = 500.0
    private val parachuteTerminalVelocity = 6.4 // m/s controlled descent
    private val windSpeedMs = 1.8 // gentle atmospheric drift
    private val windHeadingRad = 45.0 * (PI / 180.0) // North-East drift

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun startDemoMission() {
        if (_isRunning.value && !_isPaused.value) return
        if (_isPaused.value) {
            _isPaused.value = false
            return
        }

        reset()
        _isRunning.value = true
        _isPaused.value = false

        job = scope.launch(Dispatchers.Default) {
            runSimulationLoop()
        }
    }

    fun pause() {
        if (_isRunning.value) {
            _isPaused.value = true
        }
    }

    fun resume() {
        if (_isRunning.value && _isPaused.value) {
            _isPaused.value = false
        }
    }

    fun reset() {
        job?.cancel()
        job = null
        _isRunning.value = false
        _isPaused.value = false
        currentPhase = MissionPhase.STANDBY
        packetCounter = 0
        simTimeSeconds = 0.0
        altitudeM = 0.0
        velocityMs = 0.0
        batteryVoltage = 8.38
        batteryCapacity = 100.0
        currentLat = baseLat
        currentLon = baseLon

        // Emit initial Standby packet
        val initialPacket = buildTelemetryPacket(
            phase = MissionPhase.STANDBY,
            alt = 0.5,
            vel = 0.0,
            hasGps = true
        )
        scope.launch {
            _packetFlow.emit(initialPacket)
            _eventFlow.emit(
                MissionEvent(
                    timestamp = System.currentTimeMillis(),
                    phase = MissionPhase.STANDBY,
                    message = "AetherView Advaita system in STANDBY on Launch Pad. Ready for arming.",
                    altitude_m = 0.5
                )
            )
        }
    }

    private suspend fun CoroutineScope.runSimulationLoop() {
        val dt = 0.5 // 2Hz telemetry update rate (every 500ms)
        var phaseTimer = 0.0

        // Step 1: STANDBY for 3 seconds
        currentPhase = MissionPhase.STANDBY
        emitEvent("CanSat telemetry initialized. Self-diagnostics nominal.")
        while (phaseTimer < 3.0 && isActive) {
            if (!_isPaused.value) {
                phaseTimer += dt
                simTimeSeconds += dt
                emitCurrentState()
            }
            delay(500)
        }

        // Step 2: ARMED for 3 seconds
        currentPhase = MissionPhase.ARMED
        emitEvent("Rocket payload armed. Flight computer telemetry locked.")
        phaseTimer = 0.0
        while (phaseTimer < 3.0 && isActive) {
            if (!_isPaused.value) {
                phaseTimer += dt
                simTimeSeconds += dt
                altitudeM = 0.5 + (Random.nextDouble(-0.1, 0.1))
                velocityMs = 0.0
                emitCurrentState()
            }
            delay(500)
        }

        // Step 3: DEPLOYED at ~500m
        currentPhase = MissionPhase.DEPLOYED
        altitudeM = deploymentTargetAltitude
        velocityMs = -2.5
        emitEvent("Deployment apogee detected! Ejection at ${String.format(Locale.US, "%.1f", altitudeM)}m AGL.")
        phaseTimer = 0.0
        while (phaseTimer < 2.0 && isActive) {
            if (!_isPaused.value) {
                phaseTimer += dt
                simTimeSeconds += dt
                emitCurrentState()
            }
            delay(500)
        }

        // Step 4: DESCENT with parachute
        currentPhase = MissionPhase.DESCENT
        emitEvent("Parachute deployed successfully. Controlled aerodynamic descent established.")
        while (altitudeM > 2.0 && isActive) {
            if (!_isPaused.value) {
                simTimeSeconds += dt
                // Parachute aerodynamics: approaches terminal velocity ~6.4 m/s with natural gusts
                val gust = Random.nextDouble(-0.4, 0.4)
                velocityMs = -(parachuteTerminalVelocity + gust)
                altitudeM = max(0.0, altitudeM + (velocityMs * dt))

                // GPS Drift with wind: ~1.8 m/s North-East
                val dLat = (windSpeedMs * dt * cos(windHeadingRad)) / 111320.0
                val dLon = (windSpeedMs * dt * sin(windHeadingRad)) / (111320.0 * cos(baseLat * PI / 180.0))
                currentLat += dLat
                currentLon += dLon

                // Battery gentle discharge
                batteryVoltage = max(7.90, batteryVoltage - 0.0003)

                emitCurrentState()
            }
            delay(500)
        }

        // Step 5: LANDING DETECTED
        currentPhase = MissionPhase.LANDING_DETECTED
        altitudeM = 0.2
        velocityMs = 0.0
        emitEvent("Touchdown detected! Accelerometer impact registered. Parachute collapsed.")
        phaseTimer = 0.0
        while (phaseTimer < 4.0 && isActive) {
            if (!_isPaused.value) {
                phaseTimer += dt
                simTimeSeconds += dt
                emitCurrentState()
            }
            delay(500)
        }

        // Step 6: RECOVERY BEACON
        currentPhase = MissionPhase.RECOVERY
        emitEvent("Recovery audio-visual buzzer and GPS coordinates beacon activated. Ready for retrieval.")
        while (isActive) {
            if (!_isPaused.value) {
                simTimeSeconds += dt
                emitCurrentState()
            }
            delay(1000) // Lower telemetry rate on ground recovery
        }
    }

    private suspend fun emitCurrentState() {
        packetCounter++
        val packet = buildTelemetryPacket(
            phase = currentPhase,
            alt = altitudeM,
            vel = velocityMs,
            hasGps = true
        )
        _packetFlow.emit(packet)
    }

    private suspend fun emitEvent(msg: String) {
        _eventFlow.emit(
            MissionEvent(
                timestamp = System.currentTimeMillis(),
                phase = currentPhase,
                message = msg,
                altitude_m = altitudeM
            )
        )
    }

    private fun buildTelemetryPacket(
        phase: MissionPhase,
        alt: Double,
        vel: Double,
        hasGps: Boolean
    ): TelemetryPacket {
        val now = System.currentTimeMillis()
        val iso = isoFormat.format(Date(now))

        // Barometric atmosphere model (US Standard Atmosphere)
        // Sea level P0 = 1013.25 hPa, T0 = 28.5 °C (83.3 °F warm tropical ground)
        val groundTempC = 28.5
        val tempLapseRate = 0.0065 // -6.5 °C per 1000m
        val noiseTemp = Random.nextDouble(-0.15, 0.15)
        val tempC = groundTempC - (tempLapseRate * alt) + noiseTemp

        // Barometric pressure formula
        val p0 = 1013.25
        val molarMassAir = 0.0289644
        val g = 9.80665
        val rUniversal = 8.31447
        val tempK = 273.15 + tempC
        val pressureHpa = p0 * exp((-molarMassAir * g * alt) / (rUniversal * tempK)) + Random.nextDouble(-0.2, 0.2)

        // Relative Humidity: increases slightly near mid-alt layer, baseline 58%
        val humidity = min(95.0, max(40.0, 58.0 + (alt * 0.02) + Random.nextDouble(-1.2, 1.2)))

        // AQI: Clean air higher up (AQI 32 at 500m), slightly higher particulate near ground (AQI 54)
        val aqiValue = max(25, (54.0 - (alt * 0.04) + Random.nextDouble(-2.0, 2.0)).toInt())

        // RF LoRa signal calculation (Path loss increases with altitude/distance)
        val distanceM = Math.sqrt(alt * alt + 2500.0) // slant range to ground station
        val rssiDbm = (-48 - (15.0 * Math.log10(distanceM / 10.0)) + Random.nextDouble(-2.0, 2.0)).toInt()
        val snrDb = min(12.0f, max(4.0f, (11.0f - (alt.toFloat() * 0.012f) + Random.nextFloat() * 1.5f)))

        // GPS coordinates
        val gpsData = if (hasGps) {
            GpsData(
                latitude = currentLat,
                longitude = currentLon,
                altitude_m = alt + 12.0, // MSL altitude
                satellites = 10,
                hdop = 0.9f,
                valid = true
            )
        } else {
            GpsData(valid = false)
        }

        // Construct raw hex payload representation (LoRa packet frame)
        val hexPayload = buildString {
            append("24") // '$'
            append("414554") // 'AET'
            append(String.format(Locale.US, "%04X", packetCounter % 65536))
            append(String.format(Locale.US, "%02X", phase.stepNumber))
            append(String.format(Locale.US, "%04X", (alt * 10).toInt().coerceIn(0, 65535)))
            append(String.format(Locale.US, "%04X", ((tempC + 40) * 100).toInt().coerceIn(0, 65535)))
            append(String.format(Locale.US, "%04X", (pressureHpa * 10).toInt().coerceIn(0, 65535)))
            append(String.format(Locale.US, "%02X", humidity.toInt().coerceIn(0, 100)))
            append(String.format(Locale.US, "%02X", aqiValue.coerceIn(0, 255)))
            append(String.format(Locale.US, "%02X", (batteryVoltage * 10).toInt().coerceIn(0, 255)))
            append("AA55") // CRC / Frame delimiter
        }

        return TelemetryPacket(
            device_id = "AETHER-CAN-01",
            packet_id = packetCounter,
            timestamp = now,
            iso_timestamp = iso,
            mission_phase = phase,
            altitude_m = alt,
            temperature_c = tempC,
            pressure_hpa = pressureHpa,
            humidity_percent = humidity,
            aqi = aqiValue,
            descent_velocity_ms = vel,
            battery_voltage = batteryVoltage,
            gps = gpsData,
            communication = CommData(
                rssi = rssiDbm,
                snr = snrDb,
                packet_valid = true,
                crc_ok = true,
                frequency_mhz = 433.000,
                spreading_factor = 7,
                bandwidth_khz = 125,
                coding_rate = "4/5"
            ),
            raw_hex_payload = hexPayload,
            is_simulated = true
        )
    }
}
