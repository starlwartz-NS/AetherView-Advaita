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
import com.example.model.TelemetryPacket
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
import java.util.Locale

@Composable
fun CommunicationStatsCard(
    latestPacket: TelemetryPacket?,
    flightAnalytics: FlightAnalytics,
    modifier: Modifier = Modifier
) {
    val comm = latestPacket?.communication
    val rssi = comm?.rssi
    val snr = comm?.snr

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("communication_stats_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LORA RF COMMUNICATION STATS",
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "433.0 MHz SF7",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = AeroSuccess
                    )
                }
            }

            // Metric pills in 2x2 grid
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AtmoMetricItem(
                        label = "SIGNAL (RSSI)",
                        value = rssi?.let { "$it dBm" } ?: "NO SIGNAL",
                        color = if (rssi != null && rssi > -85) AeroCyan else AeroWarning,
                        modifier = Modifier.weight(1f)
                    )
                    AtmoMetricItem(
                        label = "SNR NOISE RATIO",
                        value = snr?.let { String.format(Locale.US, "%.1f dB", it) } ?: "NO SIGNAL",
                        color = AeroElectricBlue,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AtmoMetricItem(
                        label = "PACKET SUCCESS (PRR)",
                        value = String.format(Locale.US, "%.1f %%", flightAnalytics.packet_reception_rate),
                        color = AeroSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    AtmoMetricItem(
                        label = "TOTAL RX / LOST",
                        value = "${flightAnalytics.total_packets_received} / ${flightAnalytics.total_packets_lost}",
                        color = AeroTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
