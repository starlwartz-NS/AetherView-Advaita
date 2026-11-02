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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
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
import com.example.model.FlightAnalytics
import com.example.model.GroundStationConfig
import com.example.model.TelemetryPacket
import com.example.ui.components.PacketLogStreamView
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
 * Professional Desktop Communication Screen (LoRa RF link metrics & packet console)
 */
@Composable
fun CommunicationScreen(
    latestPacket: TelemetryPacket?,
    recentPackets: List<TelemetryPacket>,
    flightAnalytics: FlightAnalytics,
    config: GroundStationConfig,
    modifier: Modifier = Modifier
) {
    val comm = latestPacket?.communication
    val rssi = comm?.rssi
    val snr = comm?.snr

    val timeFormat = SimpleDateFormat("HH:mm:ss.SSS 'UTC'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("desktop_communication_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Desktop Split Row: RF Metrics (Left) + Live Packet Stream (Right)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Column: RF Link Parameters, Metrics, & Modulation Details
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AeroBgCard)
                    .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("lora_rf_status_card")
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
                                imageVector = Icons.Default.CellTower,
                                contentDescription = "LoRa Telemetry RF",
                                tint = AeroCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LORA TELEMETRY LINK & RF STATUS",
                                style = MaterialTheme.typography.labelSmall,
                                color = AeroTextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AeroSuccess)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LINK NOMINAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AeroSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Key RF Parameters Grid: RSSI, SNR, PRR %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RfPill(
                            title = "SIGNAL (RSSI)",
                            value = rssi?.let { "$it dBm" } ?: "NO SIGNAL",
                            subtext = when {
                                rssi == null -> "Awaiting frame"
                                rssi > -60 -> "Strong signal"
                                rssi > -85 -> "Nominal"
                                else -> "Weak link"
                            },
                            color = if (rssi != null && rssi > -85) AeroCyan else AeroWarning,
                            modifier = Modifier.weight(1f)
                        )

                        RfPill(
                            title = "SNR RATIO",
                            value = snr?.let { String.format(Locale.US, "%.1f dB", it) } ?: "NO SIGNAL",
                            subtext = if (snr != null && snr > 5.0) "Clean channel" else "Noise floor",
                            color = AeroElectricBlue,
                            modifier = Modifier.weight(1f)
                        )

                        RfPill(
                            title = "SUCCESS (PRR)",
                            value = String.format(Locale.US, "%.1f %%", flightAnalytics.packet_reception_rate),
                            subtext = "${flightAnalytics.total_packets_received} RX | ${flightAnalytics.total_packets_lost} Lost",
                            color = AeroSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // RF Modulation Details Strip
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AeroBgMain, RoundedCornerShape(6.dp))
                            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("TRANSCEIVER HARDWARE:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text("Semtech SX1278 (SPI) + ESP32 Gateway", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroCyan)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("CARRIER FREQUENCY:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text("${config.loraFrequencyMhz} MHz (UHF ISM)", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroTextPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("MODULATION CONFIG:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text("SF${config.loraSpreadingFactor} | BW ${config.loraBandwidthKhz}kHz | CR ${config.loraCodingRate}", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroTextSecondary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("UPDATE FREQUENCY:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text("${config.telemetryRateHz} Hz (Every ${(1000 / config.telemetryRateHz)}ms)", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroSuccess)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("LAST PACKET RECEIVED:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text(
                                latestPacket?.timestamp?.let { timeFormat.format(Date(it)) } ?: "None",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextSecondary
                            )
                        }
                    }
                }
            }

            // Right Column: Live Scrolling Packet Log Stream View
            Box(modifier = Modifier.weight(1.3f)) {
                PacketLogStreamView(
                    packets = recentPackets,
                    maxItems = 60,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RfPill(
    title: String,
    value: String,
    subtext: String,
    color: Color,
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
            Text(title, style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                subtext,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 7.sp,
                color = AeroTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
