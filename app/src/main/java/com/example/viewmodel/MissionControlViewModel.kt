package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BatteryCondition
import com.example.model.FlightAnalytics
import com.example.model.GroundStationConfig
import com.example.model.HardwareStatus
import com.example.model.MissionEvent
import com.example.model.MissionPhase
import com.example.model.OperatingMode
import com.example.model.SystemHealth
import com.example.model.TelemetryPacket
import com.example.service.CsvExporter
import com.example.service.LiveHardwareAdapter
import com.example.service.SimulationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class NavigationDestination(val title: String, val shortLabel: String) {
    OVERVIEW("Mission Overview", "Overview"),
    TELEMETRY("Live Telemetry", "Telemetry"),
    ANALYTICS("Flight Analytics", "Analytics"),
    TRACKING("Live Tracking", "Tracking"),
    COMMUNICATION("Communication", "Comm"),
    LOGS("Mission Logs", "Logs"),
    SETTINGS("System Settings", "Settings")
}

class MissionControlViewModel : ViewModel() {

    private val _currentDestination = MutableStateFlow(NavigationDestination.OVERVIEW)
    val currentDestination: StateFlow<NavigationDestination> = _currentDestination.asStateFlow()

    private val _config = MutableStateFlow(GroundStationConfig())
    val config: StateFlow<GroundStationConfig> = _config.asStateFlow()

    private val _latestPacket = MutableStateFlow<TelemetryPacket?>(null)
    val latestPacket: StateFlow<TelemetryPacket?> = _latestPacket.asStateFlow()

    private val _telemetryHistory = MutableStateFlow<List<TelemetryPacket>>(emptyList())
    val telemetryHistory: StateFlow<List<TelemetryPacket>> = _telemetryHistory.asStateFlow()

    private val _allMissionLogs = MutableStateFlow<List<TelemetryPacket>>(emptyList())
    val allMissionLogs: StateFlow<List<TelemetryPacket>> = _allMissionLogs.asStateFlow()

    private val _recentPackets = MutableStateFlow<List<TelemetryPacket>>(emptyList())
    val recentPackets: StateFlow<List<TelemetryPacket>> = _recentPackets.asStateFlow()

    private val _systemHealth = MutableStateFlow(SystemHealth())
    val systemHealth: StateFlow<SystemHealth> = _systemHealth.asStateFlow()

    private val _flightAnalytics = MutableStateFlow(FlightAnalytics())
    val flightAnalytics: StateFlow<FlightAnalytics> = _flightAnalytics.asStateFlow()

    private val _missionEvents = MutableStateFlow<List<MissionEvent>>(emptyList())
    val missionEvents: StateFlow<List<MissionEvent>> = _missionEvents.asStateFlow()

    private val _isMonitoringPaused = MutableStateFlow(false)
    val isMonitoringPaused: StateFlow<Boolean> = _isMonitoringPaused.asStateFlow()

    private val _chartTimeWindowSec = MutableStateFlow(60) // Default 60s
    val chartTimeWindowSec: StateFlow<Int> = _chartTimeWindowSec.asStateFlow()

    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    private val _logPhaseFilter = MutableStateFlow<MissionPhase?>(null)
    val logPhaseFilter: StateFlow<MissionPhase?> = _logPhaseFilter.asStateFlow()

    // Engines
    val simulationEngine = SimulationEngine(viewModelScope)
    val liveHardwareAdapter = LiveHardwareAdapter(viewModelScope)

    val isSimulationRunning = simulationEngine.isRunning
    val isSimulationPaused = simulationEngine.isPaused

    // Filtered logs flow for search & phase
    val filteredLogs: StateFlow<List<TelemetryPacket>> = combine(
        _allMissionLogs,
        _logSearchQuery,
        _logPhaseFilter
    ) { logs, query, phase ->
        logs.filter { packet ->
            val matchesPhase = (phase == null || packet.mission_phase == phase)
            val matchesQuery = if (query.isBlank()) true else {
                packet.packet_id.toString().contains(query, ignoreCase = true) ||
                    packet.mission_phase.name.contains(query, ignoreCase = true) ||
                    packet.iso_timestamp.contains(query, ignoreCase = true) ||
                    (packet.altitude_m?.toString() ?: "").contains(query)
            }
            matchesPhase && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Collect simulation packets
        viewModelScope.launch {
            simulationEngine.packetFlow.collect { packet ->
                if (_config.value.operatingMode == OperatingMode.SIMULATION) {
                    processIncomingPacket(packet)
                }
            }
        }

        // Collect simulation events
        viewModelScope.launch {
            simulationEngine.eventFlow.collect { event ->
                val updated = _missionEvents.value.toMutableList()
                updated.add(0, event)
                _missionEvents.value = updated
            }
        }

        // Collect live hardware packets
        viewModelScope.launch {
            liveHardwareAdapter.incomingPackets.collect { packet ->
                if (_config.value.operatingMode == OperatingMode.LIVE_HARDWARE) {
                    processIncomingPacket(packet)
                }
            }
        }

        // Reset to initial state
        simulationEngine.reset()
    }

    fun navigateTo(dest: NavigationDestination) {
        _currentDestination.value = dest
    }

    fun startDemoMission() {
        if (_config.value.operatingMode != OperatingMode.SIMULATION) {
            _config.value = _config.value.copy(operatingMode = OperatingMode.SIMULATION)
        }
        simulationEngine.startDemoMission()
    }

    fun pauseSimulation() {
        simulationEngine.pause()
    }

    fun resumeSimulation() {
        simulationEngine.resume()
    }

    fun resetMission() {
        simulationEngine.reset()
        _telemetryHistory.value = emptyList()
        _allMissionLogs.value = emptyList()
        _recentPackets.value = emptyList()
        _flightAnalytics.value = FlightAnalytics()
        _missionEvents.value = emptyList()
    }

    fun toggleMonitoringPause() {
        _isMonitoringPaused.value = !_isMonitoringPaused.value
    }

    fun setChartTimeWindow(sec: Int) {
        _chartTimeWindowSec.value = sec
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun setLogPhaseFilter(phase: MissionPhase?) {
        _logPhaseFilter.value = phase
    }

    fun switchOperatingMode(mode: OperatingMode) {
        _config.value = _config.value.copy(operatingMode = mode)
        if (mode == OperatingMode.LIVE_HARDWARE) {
            simulationEngine.pause()
            liveHardwareAdapter.connect(_config.value)
        } else {
            liveHardwareAdapter.disconnect()
        }
    }

    fun updateConfig(newConfig: GroundStationConfig) {
        val oldMode = _config.value.operatingMode
        _config.value = newConfig
        if (newConfig.operatingMode != oldMode) {
            switchOperatingMode(newConfig.operatingMode)
        }
    }

    fun exportCsv(context: Context) {
        CsvExporter.exportAndShareCsv(context, _allMissionLogs.value)
    }

    private fun processIncomingPacket(packet: TelemetryPacket) {
        if (_isMonitoringPaused.value) return

        _latestPacket.value = packet

        // Update history (cap at 600 points for smooth Canvas charting)
        val history = _telemetryHistory.value.toMutableList()
        history.add(packet)
        if (history.size > 600) {
            history.removeAt(0)
        }
        _telemetryHistory.value = history

        // All logs for export and tables
        val allLogs = _allMissionLogs.value.toMutableList()
        allLogs.add(0, packet) // most recent first for table
        _allMissionLogs.value = allLogs

        // Recent packets stream for communication page (last 60)
        val stream = _recentPackets.value.toMutableList()
        stream.add(0, packet)
        if (stream.size > 60) {
            stream.removeAt(stream.lastIndex)
        }
        _recentPackets.value = stream

        updateAnalyticsAndHealth(packet)
    }

    private fun updateAnalyticsAndHealth(latest: TelemetryPacket) {
        val currentAnalytics = _flightAnalytics.value
        val alt = latest.altitude_m ?: 0.0

        val maxAlt = max(currentAnalytics.max_altitude_m, alt)
        val minAlt = if (currentAnalytics.min_altitude_m == 0.0) alt else min(currentAnalytics.min_altitude_m, alt)

        val totalReceived = currentAnalytics.total_packets_received + 1
        val isSim = latest.is_simulated
        val simulatedLoss = if (isSim && totalReceived % 45L == 0L) 1L else 0L
        val totalLost = currentAnalytics.total_packets_lost + simulatedLoss

        val prr = if (totalReceived + totalLost > 0) {
            (totalReceived.toDouble() / (totalReceived + totalLost).toDouble()) * 100.0
        } else 100.0

        val flightDuration = if (latest.mission_phase == MissionPhase.DESCENT || latest.mission_phase == MissionPhase.DEPLOYED || latest.mission_phase == MissionPhase.LANDING_DETECTED || latest.mission_phase == MissionPhase.RECOVERY) {
            currentAnalytics.flight_duration_sec + 1
        } else {
            currentAnalytics.flight_duration_sec
        }

        val descentRate = abs(latest.descent_velocity_ms ?: 0.0)

        _flightAnalytics.value = currentAnalytics.copy(
            max_altitude_m = maxAlt,
            min_altitude_m = minAlt,
            flight_duration_sec = flightDuration,
            total_packets_received = totalReceived,
            total_packets_lost = totalLost,
            packet_reception_rate = prr,
            current_descent_rate_ms = descentRate,
            avg_descent_velocity_ms = if (descentRate > 0.5) (currentAnalytics.avg_descent_velocity_ms * 0.9 + descentRate * 0.1) else currentAnalytics.avg_descent_velocity_ms,
            deployment_timestamp = if (latest.mission_phase == MissionPhase.DEPLOYED && currentAnalytics.deployment_timestamp == null) latest.timestamp else currentAnalytics.deployment_timestamp,
            landing_timestamp = if (latest.mission_phase == MissionPhase.LANDING_DETECTED && currentAnalytics.landing_timestamp == null) latest.timestamp else currentAnalytics.landing_timestamp
        )

        // System Health
        val battV = latest.battery_voltage ?: 8.0
        val battCond = when {
            battV >= 8.0 -> BatteryCondition.OPTIMAL
            battV >= 7.4 -> BatteryCondition.NOMINAL
            battV >= 7.0 -> BatteryCondition.LOW
            else -> BatteryCondition.CRITICAL
        }

        val hwStatus = if (latest.is_simulated) HardwareStatus.SIMULATED else HardwareStatus.ONLINE

        _systemHealth.value = SystemHealth(
            esp32_status = hwStatus,
            lora_status = hwStatus,
            bmp280_available = true,
            bme680_available = true,
            mpu6050_available = true,
            pms5003_available = true,
            gps_lock = latest.gps.valid,
            battery_condition = battCond,
            is_data_stale = false,
            last_packet_time_ms = latest.timestamp
        )
    }
}
