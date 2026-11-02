package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroElectricBlue
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.viewmodel.NavigationDestination

fun getNavIcon(destination: NavigationDestination): ImageVector {
    return when (destination) {
        NavigationDestination.OVERVIEW -> Icons.Default.Dashboard
        NavigationDestination.TELEMETRY -> Icons.Default.Timeline
        NavigationDestination.ANALYTICS -> Icons.Default.Analytics
        NavigationDestination.TRACKING -> Icons.Default.MyLocation
        NavigationDestination.COMMUNICATION -> Icons.Default.Router
        NavigationDestination.LOGS -> Icons.Default.FormatListBulleted
        NavigationDestination.SETTINGS -> Icons.Default.Settings
    }
}

/**
 * Horizontal scrollable aerospace navigation bar for mobile and adaptive layouts
 */
@Composable
fun AerospaceBottomNavBar(
    currentDestination: NavigationDestination,
    onNavigate: (NavigationDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AeroBgMain,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AeroBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    val bg = if (isSelected) AeroBgCard else Color.Transparent
                    val border = if (isSelected) AeroCyan.copy(alpha = 0.6f) else Color.Transparent
                    val iconColor = if (isSelected) AeroCyan else AeroTextSecondary
                    val textColor = if (isSelected) AeroTextPrimary else AeroTextSecondary

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, border, RoundedCornerShape(8.dp))
                            .clickable { onNavigate(dest) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("nav_item_${dest.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getNavIcon(dest),
                            contentDescription = dest.title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dest.shortLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }

            // System Connection Footer Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(AeroSuccess)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GROUND STATION: ONLINE  |  RF: 433.000 MHz (SF7/BW125)",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = AeroTextMuted
                    )
                }

                Text(
                    text = "AETHERVIEW ADVAITA",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = AeroTextMuted
                )
            }
        }
    }
}
