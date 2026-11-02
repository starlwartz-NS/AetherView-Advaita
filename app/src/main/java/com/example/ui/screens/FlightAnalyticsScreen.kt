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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlightAnalytics
import com.example.model.MissionEvent
import com.example.model.TelemetryPacket
import com.example.ui.components.AerospaceTelemetryChart
import com.example.ui.components.ChartPoint
import com.example.ui.theme.AeroAltColor
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroPressureColor
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroVelColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Professional Desktop Flight Analytics Screen
 */
@Composable
fun FlightAnalyticsScreen(
    analytics: FlightAnalytics,
    history: List<TelemetryPacket>,
    events: List<MissionEvent>,
    modifier: Modifier = Modifier
) {
    val altPoints = history.map { ChartPoint(it.timestamp, it.altitude_m) }
    val velPoints = history.map { ChartPoint(it.timestamp, it.descent_velocity_ms?.let { v -> abs(v) }) }
    val pressPoints = history.map { ChartPoint(it.timestamp, it.pressure_hpa) }

    val timeFormat = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_flight_analytics_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section: KPI Performance Summary Cards across full width
        FlightKpiSummaryGrid(analytics = analytics)

        // Row 2: Two Wide Side-by-Side Analysis Charts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AerospaceTelemetryChart(
                title = "Descent Trajectory Profile (Altitude vs Time)",
                unit = "m",
                dataPoints = altPoints,
                lineColor = AeroAltColor,
                timeWindowSec = 0, // All mission data
                testTagKey = "analytics_chart_altitude",
                modifier = Modifier.weight(1f)
            )

            AerospaceTelemetryChart(
                title = "Descent Velocity Stability Profile",
                unit = "m/s",
                dataPoints = velPoints,
                lineColor = AeroVelColor,
                timeWindowSec = 0,
                testTagKey = "analytics_chart_velocity",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Pressure Correlation Chart (Left) + Mission Event Log (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AerospaceTelemetryChart(
                title = "Atmospheric Pressure vs Time Curve",
                unit = "hPa",
                dataPoints = pressPoints,
                lineColor = AeroPressureColor,
                timeWindowSec = 0,
                testTagKey = "analytics_chart_pressure",
                modifier = Modifier.weight(1.2f)
            )

            // Mission Events Timeline Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AeroBgCard)
                    .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
                    .testTag("mission_events_timeline")
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MISSION TIMELINE & EVENT MARKERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${events.size} RECORDED",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = AeroTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (events.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No flight events recorded yet.\nStart demo simulation to log markers.",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(events) { event ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AeroBgMain, RoundedCornerShape(4.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flag,
                                        contentDescription = "Event",
                                        tint = AeroCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.message,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = AeroTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "${event.phase.displayName} | Alt: ${event.altitude_m?.let { String.format(Locale.US, "%.1f m", it) } ?: "--"} | ${timeFormat.format(Date(event.timestamp))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            color = AeroTextMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FlightKpiSummaryGrid(analytics: FlightAnalytics) {
    val durationMins = analytics.flight_duration_sec / 60
    val durationSecs = analytics.flight_duration_sec % 60

    val timeFormat = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("flight_kpi_summary_grid")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "FLIGHT PERFORMANCE KPIS & APOGEE SUMMARY",
                style = MaterialTheme.typography.labelSmall,
                color = AeroTextSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KpiPill(
                    title = "MAX RECORDED APOGEE",
                    value = String.format(Locale.US, "%.1f m", analytics.max_altitude_m),
                    subtext = "Target: 500.0 m AGL",
                    color = AeroCyan,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    title = "TOUCHDOWN ALTITUDE",
                    value = String.format(Locale.US, "%.1f m", analytics.min_altitude_m),
                    subtext = "Ground level AGL",
                    color = com.example.ui.theme.AeroSuccess,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    title = "TOTAL FLIGHT TIME",
                    value = String.format(Locale.US, "%02d:%02d", durationMins, durationSecs),
                    subtext = "Ejection to Recovery",
                    color = com.example.ui.theme.AeroElectricBlue,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    title = "AVG DESCENT RATE",
                    value = String.format(Locale.US, "%.2f m/s", analytics.avg_descent_velocity_ms),
                    subtext = "Parachute Terminal Vel",
                    color = com.example.ui.theme.AeroWarning,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    title = "RF PACKET RECEPTION",
                    value = String.format(Locale.US, "%.1f %%", analytics.packet_reception_rate),
                    subtext = "${analytics.total_packets_received} RX | ${analytics.total_packets_lost} Lost",
                    color = com.example.ui.theme.AeroSuccess,
                    modifier = Modifier.weight(1f)
                )

                KpiPill(
                    title = "LANDING TIMESTAMP",
                    value = analytics.landing_timestamp?.let { timeFormat.format(Date(it)) } ?: "IN FLIGHT",
                    subtext = "Impact sensor verified",
                    color = if (analytics.landing_timestamp != null) com.example.ui.theme.AeroSuccess else AeroTextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun KpiPill(
    title: String,
    value: String,
    subtext: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AeroBgMain)
            .border(1.dp, AeroBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 8.sp,
                color = AeroTextMuted,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 7.sp,
                color = AeroTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
