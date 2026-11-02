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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelemetryPacket
import com.example.ui.theme.AeroAqiColor
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroHumidityColor
import com.example.ui.theme.AeroPressureColor
import com.example.ui.theme.AeroSuccess
import com.example.ui.theme.AeroTempColor
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import com.example.ui.theme.AeroWarning
import java.util.Locale

@Composable
fun AtmosphericConditionsPanel(
    latestPacket: TelemetryPacket?,
    modifier: Modifier = Modifier
) {
    val temp = latestPacket?.temperature_c ?: 28.5
    val pressure = latestPacket?.pressure_hpa ?: 1013.25
    val humidity = latestPacket?.humidity_percent ?: 58.0
    val aqi = latestPacket?.aqi ?: 45
    val alt = latestPacket?.altitude_m ?: 0.0

    // Air density: rho = P (Pa) / (R_specific * T_kelvin)
    val tempK = temp + 273.15
    val pressurePa = pressure * 100.0
    val airDensity = pressurePa / (287.058 * tempK)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("atmospheric_conditions_panel")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Atmosphere",
                        tint = AeroCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ATMOSPHERIC & ENVIRONMENTAL CONDITIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "BME680 / PMS5003",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = AeroTextMuted
                )
            }

            // Grid of atmospheric metrics
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AtmoMetricItem(
                        label = "AIR DENSITY (ρ)",
                        value = String.format(Locale.US, "%.3f kg/m³", airDensity),
                        color = AeroCyan,
                        modifier = Modifier.weight(1f)
                    )
                    AtmoMetricItem(
                        label = "LAPSE RATE",
                        value = "-6.5 °C/km",
                        color = AeroTempColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AtmoMetricItem(
                        label = "BARO PRESSURE",
                        value = String.format(Locale.US, "%.1f hPa", pressure),
                        color = AeroPressureColor,
                        modifier = Modifier.weight(1f)
                    )
                    AtmoMetricItem(
                        label = "RELATIVE HUMIDITY",
                        value = String.format(Locale.US, "%.1f %%", humidity),
                        color = AeroHumidityColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AtmoMetricItem(
                        label = "AIR QUALITY (AQI)",
                        value = "$aqi (${if (aqi <= 50) "Good" else "Moderate"})",
                        color = if (aqi <= 50) AeroSuccess else AeroWarning,
                        modifier = Modifier.weight(1f)
                    )
                    AtmoMetricItem(
                        label = "PRESSURE ALTITUDE",
                        value = String.format(Locale.US, "%.1f m", (1 - Math.pow(pressure / 1013.25, 0.190284)) * 44307.69),
                        color = AeroCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun AtmoMetricItem(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(AeroBgMain)
            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 7.sp, color = AeroTextMuted)
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
