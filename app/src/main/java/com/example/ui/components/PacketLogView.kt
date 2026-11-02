package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun PacketLogStreamView(
    packets: List<TelemetryPacket>,
    maxItems: Int = 40,
    modifier: Modifier = Modifier
) {
    var selectedPacket by remember { mutableStateOf<TelemetryPacket?>(null) }

    val timeFormat = remember {
        SimpleDateFormat("HH:mm:ss.SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag("packet_log_stream_view")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "REAL-TIME LORA PACKET STREAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = "${packets.size} PACKETS BUFFERED",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = AeroTextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Column Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AeroBgMain, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PKT #", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(44.dp))
                Text("UTC TIME", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(70.dp))
                Text("PHASE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(70.dp))
                Text("ALT / RSSI", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(74.dp))
                Text("CRC", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(36.dp))
            }

            // Packet Stream List
            if (packets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WAITING FOR TELEMETRY FRAMES...",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(packets.take(maxItems), key = { it.packet_id }) { packet ->
                        val isCrcOk = packet.communication.crc_ok
                        val timeStr = timeFormat.format(Date(packet.timestamp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(AeroBgMain.copy(alpha = 0.6f))
                                .clickable { selectedPacket = packet }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "#${packet.packet_id}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = AeroCyan,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(44.dp)
                            )
                            Text(
                                text = timeStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextSecondary,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = packet.mission_phase.name,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextPrimary,
                                maxLines = 1,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                text = "${packet.altitude_m?.toInt() ?: "--"}m | ${packet.communication.rssi ?: "--"}dB",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = AeroTextSecondary,
                                modifier = Modifier.width(74.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .background(
                                        if (isCrcOk) Color(0x3322C98B) else Color(0x33FF5263),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .padding(vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCrcOk) "OK" else "ERR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 7.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCrcOk) AeroSuccess else AeroCritical
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Packet Inspector Dialog
    selectedPacket?.let { pkt ->
        AlertDialog(
            onDismissRequest = { selectedPacket = null },
            title = {
                Text(
                    text = "PACKET #${pkt.packet_id} INSPECTOR",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AeroCyan
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "TIMESTAMP: ${pkt.iso_timestamp}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextSecondary
                    )
                    Text(
                        text = "PHASE: ${pkt.mission_phase.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("RAW HEX FRAME PAYLOAD:", style = MaterialTheme.typography.labelSmall, color = AeroTextMuted)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AeroBgMain, RoundedCornerShape(4.dp))
                            .border(1.dp, AeroBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = pkt.raw_hex_payload.ifEmpty { "24 41 45 54 48 45 52 ..." },
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = AeroCyan,
                            fontSize = 9.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("DECODED TELEMETRY:", style = MaterialTheme.typography.labelSmall, color = AeroTextMuted)
                    Text(
                        text = "• Altitude: ${pkt.altitude_m?.let { String.format(Locale.US, "%.2f m", it) } ?: "null"}\n" +
                            "• Temperature: ${pkt.temperature_c?.let { String.format(Locale.US, "%.2f °C", it) } ?: "null"}\n" +
                            "• Pressure: ${pkt.pressure_hpa?.let { String.format(Locale.US, "%.2f hPa", it) } ?: "null"}\n" +
                            "• Humidity: ${pkt.humidity_percent?.let { String.format(Locale.US, "%.1f %%", it) } ?: "null"}\n" +
                            "• AQI: ${pkt.aqi ?: "null"}\n" +
                            "• Battery: ${pkt.battery_voltage?.let { String.format(Locale.US, "%.3f V", it) } ?: "null"}\n" +
                            "• GPS: ${pkt.gps.latitude ?: "null"}, ${pkt.gps.longitude ?: "null"} (${pkt.gps.satellites} Sats)\n" +
                            "• LoRa RSSI: ${pkt.communication.rssi ?: "null"} dBm | SNR: ${pkt.communication.snr ?: "null"} dB",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextPrimary,
                        fontSize = 9.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedPacket = null },
                    colors = ButtonDefaults.buttonColors(containerColor = AeroCyan, contentColor = AeroBgMain)
                ) {
                    Text("CLOSE", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = AeroBgCard
        )
    }
}
