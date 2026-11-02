package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MissionPhase
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
fun MissionLogsScreen(
    logs: List<TelemetryPacket>,
    searchQuery: String,
    phaseFilter: MissionPhase?,
    onSearchChanged: (String) -> Unit,
    onPhaseFilterSelected: (MissionPhase?) -> Unit,
    onExportCsv: (Context) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("HH:mm:ss.S", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("mission_logs_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Controls Row: Search Bar & CSV Export Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AeroBgCard)
                .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search text field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        placeholder = {
                            Text("Search packet #, phase, alt...", fontSize = 11.sp, color = AeroTextMuted)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = AeroTextMuted, modifier = Modifier.size(16.dp))
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
                            .weight(1f)
                            .height(44.dp)
                            .testTag("log_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Export CSV Button
                    Button(
                        onClick = { onExportCsv(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AeroCyan,
                            contentColor = AeroBgMain
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("EXPORT CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Phase Filters (ALL + 6 Mission Phases)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("FILTER:", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)

                    // ALL option
                    val isAll = phaseFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isAll) AeroCyan else AeroBgMain)
                            .border(1.dp, if (isAll) AeroCyan else AeroBorder, RoundedCornerShape(4.dp))
                            .clickable { onPhaseFilterSelected(null) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ALL (${logs.size})",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isAll) FontWeight.Bold else FontWeight.Medium,
                            color = if (isAll) AeroBgMain else AeroTextSecondary
                        )
                    }

                    MissionPhase.entries.forEach { p ->
                        val isSelected = phaseFilter == p
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) AeroCyan else AeroBgMain)
                                .border(1.dp, if (isSelected) AeroCyan else AeroBorder, RoundedCornerShape(4.dp))
                                .clickable { onPhaseFilterSelected(p) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = p.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) AeroBgMain else AeroTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Data Table Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(AeroBgCard)
                .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Table Headers (Horizontal scroll enabled for technical breadth)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AeroBgMain, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PKT #", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(42.dp))
                    Text("TIME", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(62.dp))
                    Text("PHASE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(64.dp))
                    Text("ALT (m)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(50.dp))
                    Text("T (°C)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(44.dp))
                    Text("P (hPa)", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(52.dp))
                    Text("AQI", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(32.dp))
                    Text("BATT", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted, modifier = Modifier.width(42.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (logs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NO TELEMETRY LOGS MATCH FILTER CRITERIA",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = AeroTextMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(logs, key = { it.packet_id }) { packet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(AeroBgMain.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${packet.packet_id}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroCyan,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(42.dp)
                                )
                                Text(
                                    text = timeFormat.format(Date(packet.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextSecondary,
                                    modifier = Modifier.width(62.dp)
                                )
                                Text(
                                    text = packet.mission_phase.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextPrimary,
                                    maxLines = 1,
                                    modifier = Modifier.width(64.dp)
                                )
                                Text(
                                    text = packet.altitude_m?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextPrimary,
                                    modifier = Modifier.width(50.dp)
                                )
                                Text(
                                    text = packet.temperature_c?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextSecondary,
                                    modifier = Modifier.width(44.dp)
                                )
                                Text(
                                    text = packet.pressure_hpa?.let { String.format(Locale.US, "%.1f", it) } ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextSecondary,
                                    modifier = Modifier.width(52.dp)
                                )
                                Text(
                                    text = packet.aqi?.toString() ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroTextPrimary,
                                    modifier = Modifier.width(32.dp)
                                )
                                Text(
                                    text = packet.battery_voltage?.let { String.format(Locale.US, "%.2f", it) } ?: "--",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = AeroSuccess,
                                    modifier = Modifier.width(42.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
