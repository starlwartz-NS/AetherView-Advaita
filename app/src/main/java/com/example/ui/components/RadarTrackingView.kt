package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GpsData
import com.example.model.GroundStationConfig
import com.example.model.TelemetryPacket
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCritical
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroElectricBlue
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroWarning
import java.util.Locale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun RadarTrackingView(
    latestPacket: TelemetryPacket?,
    history: List<TelemetryPacket>,
    config: GroundStationConfig,
    modifier: Modifier = Modifier
) {
    val gps = latestPacket?.gps
    val isGpsValid = gps?.valid == true && gps.latitude != null && gps.longitude != null

    // Radar sweep rotation animation for live/searching feel
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    // Calculate relative distance and bearing from Ground Station to CanSat
    val stationLat = config.groundStationLat
    val stationLon = config.groundStationLon

    val (distanceM, bearingDeg, elevationDeg) = if (isGpsValid) {
        val lat1 = stationLat * PI / 180.0
        val lon1 = stationLon * PI / 180.0
        val lat2 = gps!!.latitude!! * PI / 180.0
        val lon2 = gps.longitude!! * PI / 180.0

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val groundDist = 6371000.0 * c // in meters

        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        val brng = (atan2(y, x) * 180.0 / PI + 360.0) % 360.0

        val altM = latestPacket.altitude_m ?: 0.0
        val slant = sqrt(groundDist * groundDist + altM * altM)
        val el = if (groundDist > 0.1) atan2(altM, groundDist) * 180.0 / PI else 90.0

        Triple(slant, brng, el)
    } else {
        Triple(0.0, 0.0, 0.0)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("live_tracking_radar_view")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGpsValid) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                        contentDescription = "GPS Status",
                        tint = if (isGpsValid) AeroSuccess else AeroWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TACTICAL RECOVERY & TRACKING RADAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isGpsValid) Color(0x3322C98B) else Color(0x33FFB547),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isGpsValid) AeroSuccess else AeroWarning,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isGpsValid) "3D GPS LOCK (${gps?.satellites ?: 0} SATS)" else "NO GPS LOCK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isGpsValid) AeroSuccess else AeroWarning,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Radar Scope Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AeroBgMain)
                    .border(1.dp, AeroBorder, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val maxRadius = minOf(w, h) * 0.44f

                    // Draw Concentric Range Rings (100m, 250m, 500m, 1000m scaled)
                    val rings = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                    for (r in rings) {
                        drawCircle(
                            color = AeroBorder.copy(alpha = 0.6f),
                            radius = maxRadius * r,
                            center = center,
                            style = Stroke(width = 1.2f)
                        )
                    }

                    // Cardinal Crosshairs
                    drawLine(
                        color = AeroBorder.copy(alpha = 0.7f),
                        start = Offset(center.x, center.y - maxRadius),
                        end = Offset(center.x, center.y + maxRadius),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = AeroBorder.copy(alpha = 0.7f),
                        start = Offset(center.x - maxRadius, center.y),
                        end = Offset(center.x + maxRadius, center.y),
                        strokeWidth = 1f
                    )

                    // Draw Rotating Radar Sweep line
                    val sweepRad = sweepAngle * (PI.toFloat() / 180f)
                    val sweepEnd = Offset(
                        center.x + maxRadius * cos(sweepRad),
                        center.y + maxRadius * sin(sweepRad)
                    )
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(AeroCyan.copy(alpha = 0.6f), Color.Transparent),
                            start = center,
                            end = sweepEnd
                        ),
                        start = center,
                        end = sweepEnd,
                        strokeWidth = 2f
                    )

                    // Ground Station Center Beacon
                    drawCircle(
                        color = AeroElectricBlue,
                        radius = 6f,
                        center = center
                    )
                    drawCircle(
                        color = AeroElectricBlue.copy(alpha = 0.3f),
                        radius = 12f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    if (isGpsValid) {
                        // Plot breadcrumb drift history
                        val validHistory = history.filter { it.gps.valid && it.gps.latitude != null && it.gps.longitude != null }
                        val maxScaleMeters = 500.0 // 500m range view

                        val trailPoints = mutableListOf<Offset>()
                        for (p in validHistory) {
                            val dLatM = (p.gps.latitude!! - stationLat) * 111320.0
                            val dLonM = (p.gps.longitude!! - stationLon) * (111320.0 * cos(stationLat * PI / 180.0))
                            val px = center.x + ((dLonM / maxScaleMeters).toFloat() * maxRadius).coerceIn(-maxRadius, maxRadius)
                            val py = center.y - ((dLatM / maxScaleMeters).toFloat() * maxRadius).coerceIn(-maxRadius, maxRadius)
                            trailPoints.add(Offset(px, py))
                        }

                        // Draw breadcrumb path
                        if (trailPoints.size >= 2) {
                            val path = Path().apply {
                                moveTo(trailPoints.first().x, trailPoints.first().y)
                                for (pt in trailPoints.drop(1)) {
                                    lineTo(pt.x, pt.y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = AeroCyan.copy(alpha = 0.45f),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Plot current CanSat Position Marker
                        val curLatM = (gps!!.latitude!! - stationLat) * 111320.0
                        val curLonM = (gps.longitude!! - stationLon) * (111320.0 * cos(stationLat * PI / 180.0))
                        val canSatX = center.x + ((curLonM / maxScaleMeters).toFloat() * maxRadius).coerceIn(-maxRadius, maxRadius)
                        val canSatY = center.y - ((curLatM / maxScaleMeters).toFloat() * maxRadius).coerceIn(-maxRadius, maxRadius)
                        val canSatPos = Offset(canSatX, canSatY)

                        // Glowing pulsing beacon
                        drawCircle(
                            color = AeroCyan.copy(alpha = 0.25f),
                            radius = 16f,
                            center = canSatPos
                        )
                        drawCircle(
                            color = AeroCyan,
                            radius = 6f,
                            center = canSatPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.5f,
                            center = canSatPos
                        )

                        // Vector line from Ground Station to CanSat
                        drawLine(
                            color = AeroCyan.copy(alpha = 0.7f),
                            start = center,
                            end = canSatPos,
                            strokeWidth = 1.5f
                        )
                    }
                }

                // If GPS is invalid, display technical placeholder overlay
                if (!isGpsValid) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AeroBgMain.copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsNotFixed,
                                contentDescription = null,
                                tint = AeroWarning,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = "GPS FIX ACQUISITION PENDING",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AeroWarning,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "NEO-6M scanning NMEA ephemeris sentences.\nPosition will render automatically upon lock.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroTextMuted,
                                fontFamily = FontFamily.Monospace,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Radar Telemetry Coordinates & Slant Range Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AeroBgMain, RoundedCornerShape(6.dp))
                    .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CANSAT GPS COORDS", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                    Text(
                        text = if (isGpsValid)
                            "${String.format(Locale.US, "%.5f", gps?.latitude)}° N\n${String.format(Locale.US, "%.5f", gps?.longitude)}° E"
                        else
                            "LAT: --.-----°\nLON: --.-----°",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        color = if (isGpsValid) AeroCyan else AeroTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("BEARING / AZIMUTH", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                    Text(
                        text = if (isGpsValid) "${String.format(Locale.US, "%.1f", bearingDeg)}°" else "--.-°",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = AeroElectricBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isGpsValid) "EL: ${String.format(Locale.US, "%.1f", elevationDeg)}°" else "EL: --°",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("SLANT RANGE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                    Text(
                        text = if (isGpsValid) "${distanceM.toInt()} m" else "--- m",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        color = AeroSuccess,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "GS: ${String.format(Locale.US, "%.3f", stationLat)}°, ${String.format(Locale.US, "%.3f", stationLon)}°",
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
