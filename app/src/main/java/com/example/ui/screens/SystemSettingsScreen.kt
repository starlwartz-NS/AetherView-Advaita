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
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GroundStationConfig
import com.example.model.HardwareConnectionProtocol
import com.example.model.OperatingMode
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroWarning

/**
 * Professional Desktop Multi-Column Settings Console
 */
@Composable
fun SystemSettingsScreen(
    config: GroundStationConfig,
    onUpdateConfig: (GroundStationConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var callsign by remember(config) { mutableStateOf(config.stationCallsign) }
    var endpoint by remember(config) { mutableStateOf(config.serverEndpoint) }
    var latStr by remember(config) { mutableStateOf(config.groundStationLat.toString()) }
    var lonStr by remember(config) { mutableStateOf(config.groundStationLon.toString()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_system_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Desktop 2-Column Grid for Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column: Operating Mode, Gateway Config & Safety Protocol
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 1: Operating Mode Selection
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "1. OPERATING MODE & DATA SOURCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val isSim = config.operatingMode == OperatingMode.SIMULATION
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSim) AeroWarning.copy(alpha = 0.15f) else AeroBgMain)
                                    .border(1.5.dp, if (isSim) AeroWarning else AeroBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateConfig(config.copy(operatingMode = OperatingMode.SIMULATION))
                                    }
                                    .padding(10.dp)
                                    .testTag("select_mode_simulation")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "SIMULATION MODE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSim) AeroWarning else AeroTextSecondary
                                    )
                                    Text(
                                        text = "Realistic 500m CanSat physics & atmospheric lapse simulation for demonstration.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = AeroTextMuted
                                    )
                                }
                            }

                            val isLive = config.operatingMode == OperatingMode.LIVE_HARDWARE
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isLive) AeroSuccess.copy(alpha = 0.15f) else AeroBgMain)
                                    .border(1.5.dp, if (isLive) AeroSuccess else AeroBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onUpdateConfig(config.copy(operatingMode = OperatingMode.LIVE_HARDWARE))
                                    }
                                    .padding(10.dp)
                                    .testTag("select_mode_live")
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "LIVE HARDWARE MODE",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isLive) AeroSuccess else AeroTextSecondary
                                    )
                                    Text(
                                        text = "Receives live telemetry packets via ESP32 LoRa Ground Station Gateway.",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 8.sp,
                                        color = AeroTextMuted
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Hardware Gateway Configuration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "2. HARDWARE GATEWAY CONFIGURATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("PROTOCOL ADAPTER:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            HardwareConnectionProtocol.entries.forEach { proto ->
                                val isSelected = config.protocol == proto
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AeroBgMain else AeroBgMain.copy(alpha = 0.5f))
                                        .border(1.dp, if (isSelected) AeroCyan else AeroBorder, RoundedCornerShape(6.dp))
                                        .clickable { onUpdateConfig(config.copy(protocol = proto)) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = proto.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSelected) AeroCyan else AeroTextSecondary
                                    )
                                    if (isSelected) {
                                        Text("ACTIVE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("GATEWAY SERVER ENDPOINT (WEBSOCKET / REST):", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            OutlinedTextField(
                                value = endpoint,
                                onValueChange = {
                                    endpoint = it
                                    onUpdateConfig(config.copy(serverEndpoint = it))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AeroCyan,
                                    unfocusedBorderColor = AeroBorder,
                                    focusedTextColor = AeroTextPrimary,
                                    unfocusedTextColor = AeroTextPrimary,
                                    focusedContainerColor = AeroBgMain,
                                    unfocusedContainerColor = AeroBgMain
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("gateway_endpoint_input")
                            )
                        }
                    }
                }

                // Section 3: Safety and Control Restrictions Compliance
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgMain)
                        .border(1.dp, AeroCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Safety Restriction",
                            tint = AeroCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SAFETY & FLIGHT-CONTROL RESTRICTION PROTOCOL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = AeroCyan,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "AetherView Advaita operates strictly as a passive, read-only telemetry monitoring and data logging system. Live actuator commands, parachute release overrides, and autonomous descent alteration commands are prohibited from this dashboard to guarantee flight safety.",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroTextMuted,
                                fontSize = 8.sp,
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            // Right Column: Ground Station Identity, Coordinates & RF Modulation
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Section 4: Ground Station Identity & Geolocation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "3. GROUND STATION IDENTITY & GEOLOCATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STATION CALLSIGN:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                OutlinedTextField(
                                    value = callsign,
                                    onValueChange = {
                                        callsign = it
                                        onUpdateConfig(config.copy(stationCallsign = it))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AeroCyan,
                                        unfocusedBorderColor = AeroBorder,
                                        focusedTextColor = AeroTextPrimary,
                                        unfocusedTextColor = AeroTextPrimary,
                                        focusedContainerColor = AeroBgMain,
                                        unfocusedContainerColor = AeroBgMain
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.height(48.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("DEPLOY TARGET (m):", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                OutlinedTextField(
                                    value = config.targetDeploymentAltitudeM.toInt().toString(),
                                    onValueChange = {
                                        val alt = it.toDoubleOrNull() ?: 500.0
                                        onUpdateConfig(config.copy(targetDeploymentAltitudeM = alt))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AeroCyan,
                                        unfocusedBorderColor = AeroBorder,
                                        focusedTextColor = AeroTextPrimary,
                                        unfocusedTextColor = AeroTextPrimary,
                                        focusedContainerColor = AeroBgMain,
                                        unfocusedContainerColor = AeroBgMain
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.height(48.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STATION LATITUDE (°N):", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                OutlinedTextField(
                                    value = latStr,
                                    onValueChange = {
                                        latStr = it
                                        val lat = it.toDoubleOrNull()
                                        if (lat != null) onUpdateConfig(config.copy(groundStationLat = lat))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AeroCyan,
                                        unfocusedBorderColor = AeroBorder,
                                        focusedTextColor = AeroTextPrimary,
                                        unfocusedTextColor = AeroTextPrimary,
                                        focusedContainerColor = AeroBgMain,
                                        unfocusedContainerColor = AeroBgMain
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.height(48.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("STATION LONGITUDE (°E):", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                                OutlinedTextField(
                                    value = lonStr,
                                    onValueChange = {
                                        lonStr = it
                                        val lon = it.toDoubleOrNull()
                                        if (lon != null) onUpdateConfig(config.copy(groundStationLon = lon))
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = AeroCyan,
                                        unfocusedBorderColor = AeroBorder,
                                        focusedTextColor = AeroTextPrimary,
                                        unfocusedTextColor = AeroTextPrimary,
                                        focusedContainerColor = AeroBgMain,
                                        unfocusedContainerColor = AeroBgMain
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.height(48.dp)
                                )
                            }
                        }
                    }
                }

                // Section 5: Telemetry Rate & RF LoRa Modulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AeroBgCard)
                        .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "4. TELEMETRY TRANSMISSION INTERVAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1 to "1 Hz (1000ms)", 2 to "2 Hz (500ms)", 4 to "4 Hz (250ms)", 10 to "10 Hz (100ms)").forEach { (hz, _) ->
                                val isSelected = config.telemetryRateHz == hz
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) AeroCyan else AeroBgMain)
                                        .border(1.dp, if (isSelected) AeroCyan else AeroBorder, RoundedCornerShape(6.dp))
                                        .clickable { onUpdateConfig(config.copy(telemetryRateHz = hz)) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$hz Hz",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) AeroBgMain else AeroTextSecondary
                                    )
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
