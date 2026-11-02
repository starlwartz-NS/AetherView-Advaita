package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.GroundStationConfig
import com.example.model.HardwareStatus
import com.example.model.OperatingMode
import com.example.model.SystemHealth
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
import com.example.viewmodel.NavigationDestination

/**
 * Desktop-first Fixed Sidebar (230px width) designed for laptop and workstation displays.
 */
@Composable
fun DesktopSidebar(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    config: GroundStationConfig,
    systemHealth: SystemHealth,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(240.dp)
            .fillMaxHeight()
            .background(AeroBgMain)
            .border(width = 1.dp, color = AeroBorder, shape = RoundedCornerShape(0.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Section: Mission Branding & Nav Links
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // AetherView Advaita Header Emblem & Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("desktop_sidebar_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, AeroCyan, CircleShape)
                        .background(AeroBgCard),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aetherview_advaita_logo),
                        contentDescription = "AetherView Advaita logo",
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "AetherView Advaita",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AeroCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "MISSION CONTROL",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AeroTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "ADVAITA | SEVA FIRST 2026",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 7.sp,
                        color = AeroTextMuted,
                        letterSpacing = 0.2.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AeroBorder)
            )

            // Navigation Destination Links
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "GROUND STATION MODULES",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = AeroTextMuted,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                NavigationDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    val bg = if (isSelected) AeroBgCard else Color.Transparent
                    val border = if (isSelected) AeroCyan.copy(alpha = 0.7f) else Color.Transparent
                    val iconTint = if (isSelected) AeroCyan else AeroTextSecondary
                    val textColor = if (isSelected) AeroTextPrimary else AeroTextSecondary

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(6.dp))
                            .clickable { onNavigate(dest) }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                            .testTag("sidebar_nav_${dest.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getNavIcon(dest),
                            contentDescription = dest.title,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = dest.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Bottom Section: Hardware & Ground Station Connection Status Indicator
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AeroBorder)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AeroBgCard)
                    .border(1.dp, AeroBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val isSim = config.operatingMode == OperatingMode.SIMULATION
                            val statusColor = if (isSim) AeroWarning else AeroSuccess
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSim) "SIMULATION ACTIVE" else "LIVE HARDWARE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    Text(
                        text = "GS: ${config.stationCallsign}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextSecondary
                    )
                    Text(
                        text = "RF: ${config.loraFrequencyMhz} MHz (SF${config.loraSpreadingFactor}/BW${config.loraBandwidthKhz})",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AeroCyan
                    )
                    Text(
                        text = "AETHERVIEW ADVAITA (Desktop)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextMuted
                    )
                }
            }
        }
    }
}
