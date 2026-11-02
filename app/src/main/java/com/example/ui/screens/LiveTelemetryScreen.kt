package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import com.example.model.TelemetryPacket
import com.example.ui.components.AerospaceTelemetryChart
import com.example.ui.components.ChartPoint
import com.example.ui.theme.AeroAltColor
import com.example.ui.theme.AeroAqiColor
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroHumidityColor
import com.example.ui.theme.AeroPressureColor
import com.example.ui.theme.AeroTempColor
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroVelColor
import com.example.ui.theme.AeroWarning
import kotlin.math.abs

/**
 * Professional Desktop Multi-Column Live Telemetry Dashboard
 */
@Composable
fun LiveTelemetryScreen(
    history: List<TelemetryPacket>,
    isMonitoringPaused: Boolean,
    timeWindowSec: Int,
    onTogglePause: () -> Unit,
    onSelectTimeWindow: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val altPoints = history.map { ChartPoint(it.timestamp, it.altitude_m) }
    val tempPoints = history.map { ChartPoint(it.timestamp, it.temperature_c) }
    val pressurePoints = history.map { ChartPoint(it.timestamp, it.pressure_hpa) }
    val humidityPoints = history.map { ChartPoint(it.timestamp, it.humidity_percent) }
    val aqiPoints = history.map { ChartPoint(it.timestamp, it.aqi?.toDouble()) }
    val velPoints = history.map { ChartPoint(it.timestamp, it.descent_velocity_ms?.let { v -> abs(v) }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_live_telemetry_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Controls Toolbar: Pause/Resume + Time Range Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AeroBgCard)
                .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onTogglePause,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isMonitoringPaused) AeroWarning else AeroBgMain,
                            contentColor = if (isMonitoringPaused) AeroBgMain else AeroCyan
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .border(1.dp, if (isMonitoringPaused) AeroWarning else AeroBorder, RoundedCornerShape(6.dp))
                            .testTag("telemetry_pause_resume_button")
                    ) {
                        Icon(
                            imageVector = if (isMonitoringPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isMonitoringPaused) "Resume" else "Pause",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isMonitoringPaused) "MONITORING PAUSED" else "LIVE FEED ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "SAMPLING RATE: 2 Hz  |  SYNCHRONIZED WITH ESP32 LORA RECEIVER",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextMuted
                    )
                }

                // Time Windows: 30s, 60s, 180s, All
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIME HORIZON:",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = AeroTextMuted
                    )

                    listOf(30, 60, 180, 0).forEach { winSec ->
                        val isSelected = timeWindowSec == winSec
                        val label = if (winSec == 0) "ALL DATA" else "${winSec}s"

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) AeroCyan else AeroBgMain)
                                .border(1.dp, if (isSelected) AeroCyan else AeroBorder, RoundedCornerShape(4.dp))
                                .clickable { onSelectTimeWindow(winSec) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AeroBgMain else AeroTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Row 1: Charts 1 & 2 (Altitude & Descent Velocity)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AerospaceTelemetryChart(
                title = "1. Altitude Profile (AGL Barometric)",
                unit = "m",
                dataPoints = altPoints,
                lineColor = AeroAltColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_altitude_vs_time",
                modifier = Modifier.weight(1f)
            )

            AerospaceTelemetryChart(
                title = "2. Descent Velocity Profile",
                unit = "m/s",
                dataPoints = velPoints,
                lineColor = AeroVelColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_velocity_vs_time",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Charts 3 & 4 (Pressure & Temperature)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AerospaceTelemetryChart(
                title = "3. Atmospheric Pressure Profile",
                unit = "hPa",
                dataPoints = pressurePoints,
                lineColor = AeroPressureColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_pressure_vs_time",
                modifier = Modifier.weight(1f)
            )

            AerospaceTelemetryChart(
                title = "4. Ambient Temperature Profile",
                unit = "°C",
                dataPoints = tempPoints,
                lineColor = AeroTempColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_temp_vs_time",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Charts 5 & 6 (Humidity & AQI)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AerospaceTelemetryChart(
                title = "5. Relative Humidity Profile",
                unit = "%",
                dataPoints = humidityPoints,
                lineColor = AeroHumidityColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_humidity_vs_time",
                modifier = Modifier.weight(1f)
            )

            AerospaceTelemetryChart(
                title = "6. Air Quality Index (AQI)",
                unit = "AQI",
                dataPoints = aqiPoints,
                lineColor = AeroAqiColor,
                timeWindowSec = timeWindowSec,
                testTagKey = "chart_aqi_vs_time",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
