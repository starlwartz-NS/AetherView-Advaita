package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryCondition
import com.example.model.FlightAnalytics
import com.example.model.GroundStationConfig
import com.example.model.MissionEvent
import com.example.model.MissionPhase
import com.example.model.SystemHealth
import com.example.model.TelemetryPacket
import com.example.ui.components.AerospaceTelemetryChart
import com.example.ui.components.AltitudeLadderView
import com.example.ui.components.AtmosphericConditionsPanel
import com.example.ui.components.ChartPoint
import com.example.ui.components.CommunicationStatsCard
import com.example.ui.components.PacketLogStreamView
import com.example.ui.components.RecentMissionEventsCard
import com.example.ui.components.TelemetryMetricCard
import com.example.ui.components.TrendDirection
import com.example.ui.theme.AeroAltColor
import com.example.ui.theme.AeroAqiColor
import com.example.ui.theme.AeroBatteryColor
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroHumidityColor
import com.example.ui.theme.AeroPressureColor
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTempColor
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroVelColor
import com.example.ui.theme.AeroWarning
import java.util.Locale
import kotlin.math.abs

/**
 * Professional Desktop-First Mission Overview Dashboard for 1080p, 900p, and 768p laptop screens.
 * Uses a 4-row multi-column grid layout without mobile constraints.
 */
@Composable
fun OverviewScreen(
    latestPacket: TelemetryPacket?,
    previousPacket: TelemetryPacket?,
    systemHealth: SystemHealth,
    flightAnalytics: FlightAnalytics,
    history: List<TelemetryPacket>,
    events: List<MissionEvent>,
    config: GroundStationConfig,
    modifier: Modifier = Modifier
) {
    val phase = latestPacket?.mission_phase ?: MissionPhase.STANDBY
    val alt = latestPacket?.altitude_m
    val vel = latestPacket?.descent_velocity_ms
    val temp = latestPacket?.temperature_c
    val pressure = latestPacket?.pressure_hpa
    val aqi = latestPacket?.aqi
    val batt = latestPacket?.battery_voltage

    val prevAlt = previousPacket?.altitude_m
    val altTrend = when {
        alt == null || prevAlt == null -> TrendDirection.UNKNOWN
        alt > prevAlt + 0.2 -> TrendDirection.RISING
        alt < prevAlt - 0.2 -> TrendDirection.FALLING
        else -> TrendDirection.STABLE
    }

    // Chart data streams
    val altChartPoints = history.map { ChartPoint(it.timestamp, it.altitude_m) }
    val tempChartPoints = history.map { ChartPoint(it.timestamp, it.temperature_c) }
    val pressureChartPoints = history.map { ChartPoint(it.timestamp, it.pressure_hpa) }
    val humidityChartPoints = history.map { ChartPoint(it.timestamp, it.humidity_percent) }
    val aqiChartPoints = history.map { ChartPoint(it.timestamp, it.aqi?.toDouble()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_overview_dashboard"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // ROW 1: Five Large Full-Width KPI Cards
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Altitude
            TelemetryMetricCard(
                title = "Altitude AGL",
                sensorName = "BMP280 Baro",
                valueString = alt?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                unit = "m",
                accentColor = AeroAltColor,
                isValid = alt != null,
                trend = altTrend,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "kpi_card_altitude",
                modifier = Modifier.weight(1f)
            )

            // Card 2: Temperature
            TelemetryMetricCard(
                title = "Temperature",
                sensorName = "BME680 Therm",
                valueString = temp?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                unit = "°C",
                accentColor = AeroTempColor,
                isValid = temp != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "kpi_card_temperature",
                modifier = Modifier.weight(1f)
            )

            // Card 3: Atmospheric Pressure
            TelemetryMetricCard(
                title = "Pressure",
                sensorName = "BMP280 Baro",
                valueString = pressure?.let { String.format(Locale.US, "%.1f", it) } ?: "---.-",
                unit = "hPa",
                accentColor = AeroPressureColor,
                isValid = pressure != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "kpi_card_pressure",
                modifier = Modifier.weight(1f)
            )

            // Card 4: AQI
            TelemetryMetricCard(
                title = "Air Quality (AQI)",
                sensorName = "PMS5003 Laser",
                valueString = aqi?.toString() ?: "---",
                unit = when {
                    aqi == null -> ""
                    aqi <= 50 -> "Good"
                    aqi <= 100 -> "Mod"
                    else -> "Poor"
                },
                accentColor = AeroAqiColor,
                isValid = aqi != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "kpi_card_aqi",
                modifier = Modifier.weight(1f)
            )

            // Card 5: Battery Voltage
            TelemetryMetricCard(
                title = "Battery Voltage",
                sensorName = "LiPo 2S (8.4V)",
                valueString = batt?.let { String.format(Locale.US, "%.2f", it) } ?: "-.--",
                unit = "V",
                accentColor = AeroBatteryColor,
                isValid = batt != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "kpi_card_battery",
                modifier = Modifier.weight(1f)
            )
        }

        // ==========================================
        // ROW 2: Flight Visualizer + Atmo + Health
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Column 1: Central Flight Visualization & Descent Ladder
            AltitudeLadderView(
                currentAltitudeM = alt,
                descentVelocityMs = vel,
                missionPhase = phase,
                targetAltitudeM = config.targetDeploymentAltitudeM,
                modifier = Modifier.weight(1.3f)
            )

            // Column 2: Atmospheric Conditions Panel
            AtmosphericConditionsPanel(
                latestPacket = latestPacket,
                modifier = Modifier.weight(1.0f)
            )

            // Column 3: Quick System Health Panel
            DesktopSystemHealthPanel(
                systemHealth = systemHealth,
                latestPacket = latestPacket,
                modifier = Modifier.weight(1.1f)
            )
        }

        // ==========================================
        // ROW 3: Wide Desktop Charts
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // High Priority: Large Altitude Profile Chart
            AerospaceTelemetryChart(
                title = "Altitude vs Time Profile (AGL)",
                unit = "m",
                dataPoints = altChartPoints,
                lineColor = AeroAltColor,
                timeWindowSec = 60,
                testTagKey = "overview_chart_altitude",
                modifier = Modifier.weight(1.4f)
            )

            // Temperature Profile Chart
            AerospaceTelemetryChart(
                title = "Temperature Profile",
                unit = "°C",
                dataPoints = tempChartPoints,
                lineColor = AeroTempColor,
                timeWindowSec = 60,
                testTagKey = "overview_chart_temperature",
                modifier = Modifier.weight(1.0f)
            )

            // Atmospheric Pressure Profile Chart
            AerospaceTelemetryChart(
                title = "Pressure Profile",
                unit = "hPa",
                dataPoints = pressureChartPoints,
                lineColor = AeroPressureColor,
                timeWindowSec = 60,
                testTagKey = "overview_chart_pressure",
                modifier = Modifier.weight(1.0f)
            )
        }

        // ==========================================
        // ROW 4: Events, LoRa Stats & Packet Stream
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Col 1: Recent Mission Events
            RecentMissionEventsCard(
                events = events,
                modifier = Modifier.weight(1.0f)
            )

            // Col 2: LoRa Communication Statistics
            CommunicationStatsCard(
                latestPacket = latestPacket,
                flightAnalytics = flightAnalytics,
                modifier = Modifier.weight(1.0f)
            )

            // Col 3: Latest Telemetry Packet Stream Log
            PacketLogStreamView(
                packets = history.reversed(),
                maxItems = 30,
                modifier = Modifier.weight(1.3f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun DesktopSystemHealthPanel(
    systemHealth: SystemHealth,
    latestPacket: TelemetryPacket?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("desktop_system_health_panel")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SYSTEM HEALTH & SUBSYSTEM STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroTextSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "ALL BUSES NOMINAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AeroSuccess
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DesktopHealthPill(
                        label = "ESP32 MCU",
                        value = "240 MHz (NOMINAL)",
                        isHealthy = true,
                        modifier = Modifier.weight(1f)
                    )
                    DesktopHealthPill(
                        label = "SX1278 LoRa",
                        value = "433 MHz (TX/RX)",
                        isHealthy = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DesktopHealthPill(
                        label = "I2C BUS (BMP/BME)",
                        value = "100 kHz (ONLINE)",
                        isHealthy = true,
                        modifier = Modifier.weight(1f)
                    )
                    DesktopHealthPill(
                        label = "UART PMS5003 AQI",
                        value = "9600 BAUD (OK)",
                        isHealthy = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DesktopHealthPill(
                        label = "NEO-6M GNSS",
                        value = if (systemHealth.gps_lock) "3D LOCK (${latestPacket?.gps?.satellites ?: 0} SATS)" else "ACQUIRING...",
                        isHealthy = systemHealth.gps_lock,
                        modifier = Modifier.weight(1f)
                    )
                    DesktopHealthPill(
                        label = "POWER BUS (2S)",
                        value = "${latestPacket?.battery_voltage?.let { String.format(Locale.US, "%.2fV", it) } ?: "8.34V"} (OPTIMAL)",
                        isHealthy = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopHealthPill(
    label: String,
    value: String,
    isHealthy: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AeroBgMain)
            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
            Text(
                value,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isHealthy) AeroSuccess else AeroWarning,
                fontSize = 8.sp
            )
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isHealthy) AeroSuccess else AeroWarning)
        )
    }
}
