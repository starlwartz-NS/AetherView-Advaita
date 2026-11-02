package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MissionPhase
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
import com.example.ui.theme.TelemetryValueLarge
import java.util.Locale
import kotlin.math.abs

@Composable
fun AltitudeLadderView(
    currentAltitudeM: Double?,
    descentVelocityMs: Double?,
    missionPhase: MissionPhase,
    targetAltitudeM: Double = 500.0,
    modifier: Modifier = Modifier
) {
    val alt = currentAltitudeM ?: 0.0
    val vel = descentVelocityMs ?: 0.0

    // Animated altitude ratio (0.0 at ground, 1.0 at 500m+)
    val animatedRatio by animateFloatAsState(
        targetValue = (alt / targetAltitudeM).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350),
        label = "altitudeRatio"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
            .testTag("flight_status_altitude_ladder")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Descent Trajectory",
                        tint = AeroCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FLIGHT STATUS & DESCENT LADDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                PhaseBadge(phase = missionPhase)
            }

            // Main Visualizer Row: Canvas Ladder on left, HUD stats on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vertical Ladder Canvas
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .fillMaxHeight()
                        .padding(vertical = 4.dp)
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val ladderX = canvasWidth * 0.45f
                        val topY = 10f
                        val bottomY = canvasHeight - 10f
                        val heightRange = bottomY - topY

                        // Draw Vertical Central Rail
                        drawLine(
                            color = AeroBorder,
                            start = Offset(ladderX, topY),
                            end = Offset(ladderX, bottomY),
                            strokeWidth = 3f
                        )

                        // Draw Altitude Ticks every 100m (0, 100, 200, 300, 400, 500)
                        val numTicks = 5
                        for (i in 0..numTicks) {
                            val tickRatio = i.toFloat() / numTicks.toFloat()
                            val tickY = bottomY - (tickRatio * heightRange)
                            val isTarget = i == numTicks
                            val isGround = i == 0

                            val tickColor = when {
                                isTarget -> AeroCyan
                                isGround -> AeroSuccess
                                else -> AeroTextMuted.copy(alpha = 0.6f)
                            }

                            // Tick Mark line
                            drawLine(
                                color = tickColor,
                                start = Offset(ladderX - 12f, tickY),
                                end = Offset(ladderX + 12f, tickY),
                                strokeWidth = if (isTarget || isGround) 2.5f else 1.5f
                            )
                        }

                        // CanSat Probe Cursor Y Position (inverted: 0m is bottom, 500m is top)
                        val cursorY = bottomY - (animatedRatio * heightRange)

                        // Glowing descent track from probe to ground
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(AeroCyan.copy(alpha = 0.8f), AeroCyan.copy(alpha = 0.1f)),
                                startY = cursorY,
                                endY = bottomY
                            ),
                            start = Offset(ladderX, cursorY),
                            end = Offset(ladderX, bottomY),
                            strokeWidth = 4f
                        )

                        // CanSat Probe Marker (Diamond / Chevron)
                        val chevronPath = Path().apply {
                            moveTo(ladderX, cursorY - 9f)
                            lineTo(ladderX + 14f, cursorY)
                            lineTo(ladderX, cursorY + 9f)
                            lineTo(ladderX - 14f, cursorY)
                            close()
                        }
                        drawPath(
                            path = chevronPath,
                            color = AeroCyan
                        )
                        drawCircle(
                            color = AeroBgMain,
                            radius = 4f,
                            center = Offset(ladderX, cursorY)
                        )

                        // Target Line Highlight at Top
                        drawLine(
                            color = AeroCyan.copy(alpha = 0.5f),
                            start = Offset(0f, topY),
                            end = Offset(canvasWidth, topY),
                            strokeWidth = 1.5f,
                            pathEffect = null
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Stats Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Current Altitude Readout
                    Column {
                        Text(
                            text = "CURRENT ALTITUDE",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextMuted,
                            fontSize = 9.sp
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = String.format(Locale.US, "%.1f", alt),
                                style = TelemetryValueLarge,
                                color = AeroCyan,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "m AGL",
                                style = MaterialTheme.typography.titleMedium,
                                color = AeroTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "≈ ${(alt * 3.28084).toInt()} ft MSL",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Descent Velocity
                    Column {
                        Text(
                            text = "VERTICAL VELOCITY",
                            style = MaterialTheme.typography.labelSmall,
                            color = AeroTextMuted,
                            fontSize = 9.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (vel < -0.2) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Descent",
                                    tint = AeroWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else if (vel > 0.2) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Ascent",
                                    tint = AeroElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.2f m/s", abs(vel)),
                                style = MaterialTheme.typography.titleLarge,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (abs(vel) > 8.0) AeroWarning else AeroTextPrimary
                            )
                        }
                    }

                    // Target deployment reference & ETA
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AeroBgMain, RoundedCornerShape(6.dp))
                            .border(1.dp, AeroBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TARGET APOGEE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text("500.0 m", style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace, color = AeroCyan, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("DESCENT RATE", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = AeroTextMuted)
                            Text(
                                if (vel < -0.5) "NOMINAL (PARACHUTE)" else "STABLE / PAD",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = if (vel < -0.5) AeroSuccess else AeroTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
