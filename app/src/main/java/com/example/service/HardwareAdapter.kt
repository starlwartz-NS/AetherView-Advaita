package com.example.service

import com.example.model.CommData
import com.example.model.GpsData
import com.example.model.GroundStationConfig
import com.example.model.HardwareStatus
import com.example.model.MissionPhase
import com.example.model.TelemetryPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

interface HardwareAdapter {
    val isConnected: StateFlow<Boolean>
    val connectionStatus: StateFlow<HardwareStatus>
    val incomingPackets: SharedFlow<TelemetryPacket>
    val statusMessage: StateFlow<String>
    fun connect(config: GroundStationConfig)
    fun disconnect()
}

/**
 * LiveHardwareAdapter handles real ESP32 / LoRa Ground Station communication.
 * Supports WebSocket telemetry stream and fallback HTTP REST polling.
 * Parses normalized JSON telemetry data contract and handles connection losses.
 */
class LiveHardwareAdapter(
    private val scope: CoroutineScope
) : HardwareAdapter {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionStatus = MutableStateFlow(HardwareStatus.OFFLINE)
    override val connectionStatus: StateFlow<HardwareStatus> = _connectionStatus.asStateFlow()

    private val _incomingPackets = MutableSharedFlow<TelemetryPacket>(extraBufferCapacity = 64)
    override val incomingPackets: SharedFlow<TelemetryPacket> = _incomingPackets.asSharedFlow()

    private val _statusMessage = MutableStateFlow("Hardware receiver disconnected.")
    override val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    private var okHttpClient: OkHttpClient? = null
    private var webSocket: WebSocket? = null
    private var pollJob: Job? = null

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    override fun connect(config: GroundStationConfig) {
        disconnect()
        _connectionStatus.value = HardwareStatus.STANDBY
        _statusMessage.value = "Connecting to ${config.serverEndpoint}..."

        okHttpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        if (config.serverEndpoint.startsWith("ws://") || config.serverEndpoint.startsWith("wss://")) {
            connectWebSocket(config.serverEndpoint)
        } else {
            startRestPolling(config.serverEndpoint, config.telemetryRateHz)
        }
    }

    private fun connectWebSocket(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = okHttpClient?.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                _connectionStatus.value = HardwareStatus.ONLINE
                _statusMessage.value = "Connected to Live ESP32 Ground Station."
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseAndEmitPacket(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                _connectionStatus.value = HardwareStatus.ERROR
                _statusMessage.value = "Hardware connection error: ${t.localizedMessage ?: "Connection refused"}"
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                _connectionStatus.value = HardwareStatus.OFFLINE
                _statusMessage.value = "Ground station disconnected."
            }
        })
    }

    private fun startRestPolling(url: String, rateHz: Int) {
        val intervalMs = (1000 / rateHz.coerceAtLeast(1)).toLong()
        pollJob = scope.launch(Dispatchers.IO) {
            _connectionStatus.value = HardwareStatus.ONLINE
            _isConnected.value = true
            _statusMessage.value = "Polling live endpoint: $url"

            while (isActive) {
                try {
                    val request = Request.Builder().url(url).build()
                    val response = okHttpClient?.newCall(request)?.execute()
                    if (response?.isSuccessful == true) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty()) {
                            parseAndEmitPacket(body)
                        }
                    } else {
                        _connectionStatus.value = HardwareStatus.DATA_STALE
                        _statusMessage.value = "Telemetry HTTP ${response?.code ?: 0}"
                    }
                } catch (e: Exception) {
                    _connectionStatus.value = HardwareStatus.ERROR
                    _statusMessage.value = "Polling error: ${e.message}"
                }
                delay(intervalMs)
            }
        }
    }

    private fun parseAndEmitPacket(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val devId = json.optString("device_id", "AETHER-CAN-01")
            val pktId = json.optLong("packet_id", System.currentTimeMillis() / 1000)
            val phaseStr = json.optString("mission_phase", "DESCENT")
            val phase = runCatching { MissionPhase.valueOf(phaseStr) }.getOrDefault(MissionPhase.DESCENT)

            val alt = if (json.has("altitude_m") && !json.isNull("altitude_m")) json.getDouble("altitude_m") else null
            val temp = if (json.has("temperature_c") && !json.isNull("temperature_c")) json.getDouble("temperature_c") else null
            val press = if (json.has("pressure_hpa") && !json.isNull("pressure_hpa")) json.getDouble("pressure_hpa") else null
            val hum = if (json.has("humidity_percent") && !json.isNull("humidity_percent")) json.getDouble("humidity_percent") else null
            val aqi = if (json.has("aqi") && !json.isNull("aqi")) json.getInt("aqi") else null
            val vel = if (json.has("descent_velocity_ms") && !json.isNull("descent_velocity_ms")) json.getDouble("descent_velocity_ms") else null
            val batt = if (json.has("battery_voltage") && !json.isNull("battery_voltage")) json.getDouble("battery_voltage") else null

            val gpsJson = json.optJSONObject("gps")
            val gpsData = if (gpsJson != null) {
                GpsData(
                    latitude = if (gpsJson.has("latitude") && !gpsJson.isNull("latitude")) gpsJson.getDouble("latitude") else null,
                    longitude = if (gpsJson.has("longitude") && !gpsJson.isNull("longitude")) gpsJson.getDouble("longitude") else null,
                    altitude_m = if (gpsJson.has("altitude") && !gpsJson.isNull("altitude")) gpsJson.getDouble("altitude") else null,
                    satellites = gpsJson.optInt("satellites", 0),
                    hdop = if (gpsJson.has("hdop")) gpsJson.getDouble("hdop").toFloat() else null,
                    valid = gpsJson.optBoolean("valid", false)
                )
            } else {
                GpsData()
            }

            val commJson = json.optJSONObject("communication")
            val commData = if (commJson != null) {
                CommData(
                    rssi = if (commJson.has("rssi") && !commJson.isNull("rssi")) commJson.getInt("rssi") else null,
                    snr = if (commJson.has("snr") && !commJson.isNull("snr")) commJson.getDouble("snr").toFloat() else null,
                    packet_valid = commJson.optBoolean("packet_valid", true),
                    crc_ok = commJson.optBoolean("crc_ok", true),
                    frequency_mhz = commJson.optDouble("frequency_mhz", 433.0),
                    spreading_factor = commJson.optInt("spreading_factor", 7),
                    bandwidth_khz = commJson.optInt("bandwidth_khz", 125)
                )
            } else {
                CommData(packet_valid = true, crc_ok = true)
            }

            val packet = TelemetryPacket(
                device_id = devId,
                packet_id = pktId,
                timestamp = System.currentTimeMillis(),
                iso_timestamp = isoFormat.format(Date()),
                mission_phase = phase,
                altitude_m = alt,
                temperature_c = temp,
                pressure_hpa = press,
                humidity_percent = hum,
                aqi = aqi,
                descent_velocity_ms = vel,
                battery_voltage = batt,
                gps = gpsData,
                communication = commData,
                raw_hex_payload = json.optString("raw_hex", ""),
                is_simulated = false
            )

            scope.launch {
                _incomingPackets.emit(packet)
            }
        } catch (e: Exception) {
            // Malformed packet
        }
    }

    override fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        pollJob?.cancel()
        pollJob = null
        okHttpClient?.dispatcher?.executorService?.shutdown()
        okHttpClient = null
        _isConnected.value = false
        _connectionStatus.value = HardwareStatus.OFFLINE
        _statusMessage.value = "Hardware disconnected."
    }
}
