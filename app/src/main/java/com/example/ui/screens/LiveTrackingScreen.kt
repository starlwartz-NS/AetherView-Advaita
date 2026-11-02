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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.SatelliteAlt
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
import com.example.model.GroundStationConfig
import com.example.model.TelemetryPacket
import com.example.ui.components.RadarTrackingView
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroElectricBlue
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Professional Desktop Split-Pane Live Tracking Console
 */
@Composable
fun LiveTrackingScreen(
    latestPacket: TelemetryPacket?,
    history: List<TelemetryPacket>,
    config: GroundStationConfig,
    modifier: Modifier = Modifier
) {
    val gps = latestPacket?.gps
    val isLock = gps?.valid == true && gps.latitude != null && gps.longitude != null

    val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_live_tracking_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main Desktop Row: Radar Console (Left) + GNSS Telemetry & Geolocation Panel (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left: Tactical Tracking Radar Scope
            RadarTrackingView(
                latestPacket = latestPacket,
                history = history,
                config = config,
                modifier = Modifier.weight(1.3f)
            )

            // Right: GNSS Ephemeris & Geolocation Detailed Telemetry Console
            Column(
                modifier = Modifier.weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Box 1: Receiver Status
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SatelliteAlt,
                                    contentDescription = "GNSS",
                                    tint = AeroCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GNSS RECEIVER & CONSTELLATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AeroTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Text(
                                text = if (isLock) "3D DGPS LOCK" else "ACQUIRING...",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (isLock) AeroSuccess else AeroWarning,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp
                            )
                        }

                        // 4 Metrics Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            InfoMetricPill(
                                title = "SATS TRACKED",
                                value = if (isLock) "${gps?.satellites ?: 0}" else "0 (Searching)",
                                color = if (isLock) AeroSuccess else AeroWarning,
                                modifier = Modifier.weight(1f)
                            )
                            InfoMetricPill(
                                title = "DILUTION (HDOP)",
                                value = if (isLock) "${gps?.hdop ?: 1.0}" else "N/A",
                                color = AeroCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            InfoMetricPill(
                                title = "GNSS ALTITUDE",
                                value = if (isLock) "${gps?.altitude_m?.toInt() ?: 0} m" else "---",
                                color = AeroElectricBlue,
                                modifier = Modifier.weight(1f)
                            )
                            InfoMetricPill(
                                title = "SIGNAL HEALTH",
                                value = if (isLock) "EXCELLENT" else "STANDBY",
                                color = if (isLock) AeroSuccess else AeroTextMuted,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Box 2: Geodetic Geolocation Coordinates & Ground Station Reference
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "GEODETIC POSITION & VECTORING",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AeroBgMain, RoundedCornerShape(6.dp))
                                .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CANSAT LATITUDE:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                Text(
                                    text = if (isLock) "${gps?.latitude}° N" else "AWAITING LOCK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isLock) AeroCyan else AeroWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CANSAT LONGITUDE:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                Text(
                                    text = if (isLock) "${gps?.longitude}° E" else "AWAITING LOCK",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isLock) AeroCyan else AeroWarning,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GS REFERENCE COORDS:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                Text(
                                    text = "${config.groundStationLat}° N, ${config.groundStationLon}° E",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("GS ELEVATION MSL:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                Text(
                                    text = "${config.groundStationElevationM} m MSL",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextSecondary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("LAST NMEA TIMESTAMP:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                Text(
                                    text = latestPacket?.timestamp?.let { timeFormat.format(Date(it)) } ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextMuted
                                )
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
fun InfoMetricPill(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AeroBgMain)
            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
