package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
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
import com.example.ui.theme.AeroAltColor
import com.example.ui.theme.AeroAqiColor
import com.example.ui.theme.AeroBatteryColor
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroBorderGlow
import com.example.ui.theme.AeroCritical
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
import com.example.ui.theme.TelemetryValueLarge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

enum class TrendDirection {
    RISING, FALLING, STABLE, UNKNOWN
}

@Composable
fun TelemetryMetricCard(
    title: String,
    sensorName: String,
    valueString: String,
    unit: String,
    accentColor: Color,
    isValid: Boolean,
    trend: TrendDirection = TrendDirection.STABLE,
    lastUpdateMs: Long? = null,
    testTagKey: String,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("HH:mm:ss.S", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val timeLabel = lastUpdateMs?.let { timeFormat.format(Date(it)) } ?: "--:--:--"

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag(testTagKey)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header: Title & Sensor + Validity indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = sensorName,
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextMuted,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Trend icon
                    when (trend) {
                        TrendDirection.RISING -> Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Rising",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        TrendDirection.FALLING -> Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Falling",
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        TrendDirection.STABLE -> Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Stable",
                            tint = AeroTextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        TrendDirection.UNKNOWN -> Unit
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Status Dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isValid) AeroSuccess else AeroWarning)
                    )
                }
            }

            // Measurement & Unit
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = valueString,
                    style = TelemetryValueLarge,
                    color = if (isValid) accentColor else AeroTextMuted,
                    fontWeight = FontWeight.Bold
                )
                if (unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.titleMedium,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            // Footer: Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isValid) "VALID DATA" else "NO SIGNAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isValid) AeroSuccess.copy(alpha = 0.8f) else AeroCritical,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "T: $timeLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = AeroTextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Standard grid of 8 primary telemetry metric cards
 */
@Composable
fun PrimaryTelemetryGrid(
    latestPacket: TelemetryPacket?,
    previousPacket: TelemetryPacket?,
    modifier: Modifier = Modifier
) {
    val alt = latestPacket?.altitude_m
    val prevAlt = previousPacket?.altitude_m
    val altTrend = when {
        alt == null || prevAlt == null -> TrendDirection.UNKNOWN
        alt > prevAlt + 0.2 -> TrendDirection.RISING
        alt < prevAlt - 0.2 -> TrendDirection.FALLING
        else -> TrendDirection.STABLE
    }

    val temp = latestPacket?.temperature_c
    val pressure = latestPacket?.pressure_hpa
    val humidity = latestPacket?.humidity_percent
    val aqi = latestPacket?.aqi
    val vel = latestPacket?.descent_velocity_ms
    val batt = latestPacket?.battery_voltage
    val gps = latestPacket?.gps

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1: Altitude & Descent Velocity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryMetricCard(
                title = "Altitude",
                sensorName = "BMP280 Baro AGL",
                valueString = alt?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                unit = "m",
                accentColor = AeroAltColor,
                isValid = alt != null,
                trend = altTrend,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_altitude",
                modifier = Modifier.weight(1f)
            )

            TelemetryMetricCard(
                title = "Descent Velocity",
                sensorName = "Inertial Kalman / Baro",
                valueString = vel?.let { String.format(Locale.US, "%.1f", abs(it)) } ?: "--.-",
                unit = "m/s",
                accentColor = AeroVelColor,
                isValid = vel != null,
                trend = when {
                    vel == null -> TrendDirection.UNKNOWN
                    vel < -0.5 -> TrendDirection.FALLING
                    vel > 0.5 -> TrendDirection.RISING
                    else -> TrendDirection.STABLE
                },
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_velocity",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Atmospheric Pressure & Temperature
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryMetricCard(
                title = "Atmospheric Pressure",
                sensorName = "BMP280 Transducer",
                valueString = pressure?.let { String.format(Locale.US, "%.1f", it) } ?: "---.-",
                unit = "hPa",
                accentColor = AeroPressureColor,
                isValid = pressure != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_pressure",
                modifier = Modifier.weight(1f)
            )

            TelemetryMetricCard(
                title = "Temperature",
                sensorName = "BME680 Thermistor",
                valueString = temp?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                unit = "°C",
                accentColor = AeroTempColor,
                isValid = temp != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_temperature",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Humidity & Air Quality Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryMetricCard(
                title = "Relative Humidity",
                sensorName = "BME680 Capacitive",
                valueString = humidity?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                unit = "%",
                accentColor = AeroHumidityColor,
                isValid = humidity != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_humidity",
                modifier = Modifier.weight(1f)
            )

            TelemetryMetricCard(
                title = "Air Quality Index (AQI)",
                sensorName = "PMS5003 Laser Particulate",
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
                testTagKey = "telemetry_card_aqi",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: Battery Voltage & GPS Coordinates
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TelemetryMetricCard(
                title = "Battery Voltage",
                sensorName = "2S LiPo Monitor (8.4V)",
                valueString = batt?.let { String.format(Locale.US, "%.2f", it) } ?: "-.--",
                unit = "V",
                accentColor = AeroBatteryColor,
                isValid = batt != null,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_battery",
                modifier = Modifier.weight(1f)
            )

            val gpsCoords = if (gps != null && gps.valid && gps.latitude != null && gps.longitude != null) {
                "${String.format(Locale.US, "%.4f", gps.latitude)}°, ${String.format(Locale.US, "%.4f", gps.longitude)}°"
            } else {
                "LOCK PENDING"
            }

            TelemetryMetricCard(
                title = "GPS Coordinates",
                sensorName = "NEO-6M Satellite Receiver",
                valueString = gpsCoords,
                unit = if (gps?.valid == true) "${gps.satellites} Sats" else "",
                accentColor = AeroCyan,
                isValid = gps?.valid == true,
                trend = TrendDirection.STABLE,
                lastUpdateMs = latestPacket?.timestamp,
                testTagKey = "telemetry_card_gps",
                modifier = Modifier.weight(1f)
            )
        }
    }
}
