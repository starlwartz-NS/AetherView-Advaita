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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FlightAnalytics
import com.example.model.MissionPhase
import com.example.model.OperatingMode
import com.example.model.TelemetryPacket
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroWarning
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Desktop-first Top Header Bar across the ground-station dashboard
 */
@Composable
fun DesktopTopHeader(
    operatingMode: OperatingMode,
    latestPacket: TelemetryPacket?,
    flightAnalytics: FlightAnalytics,
    isSimulationRunning: Boolean,
    isSimulationPaused: Boolean,
    onStartSimulation: () -> Unit,
    onPauseSimulation: () -> Unit,
    onResumeSimulation: () -> Unit,
    onResetSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeUtc by remember { mutableStateOf("") }
    var currentTimeLocal by remember { mutableStateOf("") }

    val utcFormat = remember {
        SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val localFormat = remember {
        SimpleDateFormat("HH:mm:ss", Locale.US)
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTimeUtc = utcFormat.format(now)
            currentTimeLocal = localFormat.format(now)
            delay(1000)
        }
    }

    val currentPhase = latestPacket?.mission_phase ?: MissionPhase.STANDBY
    val metSec = flightAnalytics.flight_duration_sec
    val metMins = metSec / 60
    val metSecs = metSec % 60

    Surface(
        color = AeroBgMain,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AeroBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Title, Team & Phase
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AetherView Advaita | MISSION CONTROL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = AeroCyan,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "• TEAM ADVAITA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AeroTextSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        PhaseBadge(phase = currentPhase)
                        Text(
                            text = "|",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroBorder
                        )
                        Text(
                            text = "MET: ${String.format(Locale.US, "%02d:%02d", metMins, metSecs)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = AeroCyan,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Center: Telemetry Status Banner
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Target Apogee & Current Altitude Pill
                Box(
                    modifier = Modifier
                        .background(AeroBgCard, RoundedCornerShape(6.dp))
                        .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("TARGET APOGEE", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
                            Text("500.0 m AGL", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroCyan, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("CURRENT ALTITUDE", style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
                            Text(
                                text = latestPacket?.altitude_m?.let { String.format(Locale.US, "%.1f m", it) } ?: "--.- m",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Mode Badge
                OperatingModeBadge(mode = operatingMode)
            }

            // Right: Clocks and Simulation Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Dual Timecode (UTC & Local)
                Box(
                    modifier = Modifier
                        .background(AeroBgCard, RoundedCornerShape(6.dp))
                        .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = currentTimeUtc.ifEmpty { "00:00:00 UTC" },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            color = AeroCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "LOC: ${currentTimeLocal.ifEmpty { "00:00:00" }}",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = AeroTextMuted,
                            fontSize = 8.sp
                        )
                    }
                }

                // Controls Toolbar
                if (operatingMode == OperatingMode.SIMULATION) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isSimulationRunning) {
                            Button(
                                onClick = onStartSimulation,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AeroCyan,
                                    contentColor = AeroBgMain
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("desktop_start_demo_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("START DEMO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (isSimulationPaused) {
                                FilledTonalButton(
                                    onClick = onResumeSimulation,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("desktop_resume_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESUME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onPauseSimulation,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .height(34.dp)
                                        .testTag("desktop_pause_button")
                                ) {
                                    Text("PAUSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AeroWarning)
                                }
                            }

                            OutlinedButton(
                                onClick = onResetSimulation,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("desktop_reset_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(14.dp), tint = AeroTextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AeroTextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
