package com.example.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.MissionPhase
import com.example.model.OperatingMode
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
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun AerospaceTopBar(
    operatingMode: OperatingMode,
    latestPacket: TelemetryPacket?,
    isSimulationRunning: Boolean,
    isSimulationPaused: Boolean,
    onStartSimulation: () -> Unit,
    onPauseSimulation: () -> Unit,
    onResumeSimulation: () -> Unit,
    onResetSimulation: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeString by remember { mutableStateOf("") }
    val timeFormat = remember {
        SimpleDateFormat("HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = timeFormat.format(Date())
            delay(1000)
        }
    }

    val currentPhase = latestPacket?.mission_phase ?: MissionPhase.STANDBY

    Surface(
        color = AeroBgMain,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AeroBorder, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            // Main Top Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("app_identity_header")
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, AeroCyan, CircleShape)
                            .background(AeroBgCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.aetherview_advaita_logo),
                            contentDescription = "AetherView Advaita logo",
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AetherView Advaita",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = AeroCyan,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PhaseBadge(phase = currentPhase)
                        }
                        Text(
                            text = "MISSION CONTROL  |  ADVAITA • SEVA FIRST 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Status & Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clock
                    Box(
                        modifier = Modifier
                            .background(AeroBgCard, RoundedCornerShape(6.dp))
                            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currentTimeString.ifEmpty { "00:00:00 UTC" },
                            style = MaterialTheme.typography.labelMedium,
                            fontFamily = FontFamily.Monospace,
                            color = AeroCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mode Badge
                    OperatingModeBadge(mode = operatingMode)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Control Strip for Simulation / Live
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Simulation Banner / Notice
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (operatingMode == OperatingMode.SIMULATION) AeroWarning else AeroSuccess
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (operatingMode == OperatingMode.SIMULATION)
                            "SIMULATION ENGINE ACTIVE • Target 500m AGL Descent"
                        else
                            "LIVE HARDWARE ACTIVE • LoRa Ground Station",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (operatingMode == OperatingMode.SIMULATION) AeroWarning else AeroSuccess
                    )
                }

                // Controls
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
                                    .testTag("start_demo_mission_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(16.dp))
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
                                    .testTag("resume_simulation_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESUME", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onPauseSimulation,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                    .height(34.dp)
                                    .testTag("pause_simulation_button")
                                ) {
                                    Text("PAUSE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AeroWarning)
                                }
                            }

                            OutlinedButton(
                                onClick = onResetSimulation,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("reset_simulation_button")
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

@Composable
fun PhaseBadge(phase: MissionPhase) {
    val (bgColor, textColor) = when (phase) {
        MissionPhase.STANDBY -> AeroBgCard to AeroTextMuted
        MissionPhase.ARMED -> Color(0x33FFB547) to AeroWarning
        MissionPhase.DEPLOYED -> Color(0x335271FF) to AeroElectricBlue
        MissionPhase.DESCENT -> Color(0x3300D1FF) to AeroCyan
        MissionPhase.LANDING_DETECTED -> Color(0x3322C98B) to AeroSuccess
        MissionPhase.RECOVERY -> Color(0x3300D1FF) to AeroCyan
    }

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = phase.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun OperatingModeBadge(mode: OperatingMode) {
    val isSim = mode == OperatingMode.SIMULATION
    val bg = if (isSim) Color(0x22FFB547) else Color(0x2222C98B)
    val borderCol = if (isSim) AeroWarning else AeroSuccess
    val textColor = if (isSim) AeroWarning else AeroSuccess

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .border(1.dp, borderCol.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(borderCol)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (isSim) "SIMULATION" else "LIVE HARDWARE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
            fontFamily = FontFamily.Monospace
        )
    }
}
