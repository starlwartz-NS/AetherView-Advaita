package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AeroBgCard
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.AeroBorder
import com.example.ui.theme.AeroCyan
import com.example.ui.theme.AeroTextMuted
import com.example.ui.theme.AeroTextPrimary
import com.example.ui.theme.AeroTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.min

data class ChartPoint(
    val timestampMs: Long,
    val value: Double?
)

@Composable
fun AerospaceTelemetryChart(
    title: String,
    unit: String,
    dataPoints: List<ChartPoint>,
    lineColor: Color,
    timeWindowSec: Int = 60,
    testTagKey: String,
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableStateOf<Float?>(null) }
    var inspectedPoint by remember { mutableStateOf<ChartPoint?>(null) }

    val validPoints = dataPoints.filter { it.value != null }
    val latestValue = validPoints.lastOrNull()?.value

    val minVal = if (validPoints.isNotEmpty()) validPoints.minOf { it.value!! } else 0.0
    val maxVal = if (validPoints.isNotEmpty()) validPoints.maxOf { it.value!! } else 100.0
    val effectiveMin = if (minVal == maxVal) minVal - 1.0 else minVal
    val effectiveMax = if (minVal == maxVal) maxVal + 1.0 else maxVal
    val valRange = max(0.001, effectiveMax - effectiveMin)

    val timeFormat = remember {
        SimpleDateFormat("HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AeroBgCard)
            .border(1.dp, AeroBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
            .testTag(testTagKey)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title.uppercase(Locale.US),
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Window: ${if (timeWindowSec == 0) "ALL" else "${timeWindowSec}s"} | Live 2Hz",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 8.sp,
                        color = AeroTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Current or Inspected Value Readout
                val displayVal = inspectedPoint?.value ?: latestValue
                val displayTime = inspectedPoint?.timestampMs?.let { timeFormat.format(Date(it)) }
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .background(AeroBgMain, RoundedCornerShape(4.dp))
                        .border(1.dp, AeroBorder, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = displayVal?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = lineColor
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.labelSmall,
                        color = AeroTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    if (displayTime != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "@ $displayTime",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = AeroTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Interactive Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures(
                            onPress = { offset ->
                                touchX = offset.x
                                tryAwaitRelease()
                                touchX = null
                                inspectedPoint = null
                            }
                        )
                    }
                    .pointerInput(dataPoints) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                touchX = change.position.x
                            },
                            onDragEnd = {
                                touchX = null
                                inspectedPoint = null
                            },
                            onDragCancel = {
                                touchX = null
                                inspectedPoint = null
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw Horizontal Grid lines (3 lines)
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = h * (i.toFloat() / gridLines.toFloat())
                        drawLine(
                            color = AeroBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    if (validPoints.size < 2) {
                        // Empty / Waiting state
                        return@Canvas
                    }

                    // Windowing calculation
                    val now = validPoints.last().timestampMs
                    val cutoff = if (timeWindowSec > 0) now - (timeWindowSec * 1000L) else validPoints.first().timestampMs
                    val windowPoints = validPoints.filter { it.timestampMs >= cutoff }
                    val displayList = if (windowPoints.size >= 2) windowPoints else validPoints.takeLast(20)

                    val tMin = displayList.first().timestampMs
                    val tMax = displayList.last().timestampMs
                    val tRange = max(1L, tMax - tMin).toFloat()

                    val chartPath = Path()
                    val fillPath = Path()
                    var firstPoint = true

                    val screenCoords = mutableListOf<Pair<Offset, ChartPoint>>()

                    for (p in displayList) {
                        val valDouble = p.value ?: continue
                        val x = ((p.timestampMs - tMin).toFloat() / tRange) * w
                        val yRatio = ((valDouble - effectiveMin) / valRange).toFloat().coerceIn(0f, 1f)
                        val y = h - (yRatio * (h - 10f)) - 5f // Inverted Y

                        val ptOffset = Offset(x, y)
                        screenCoords.add(ptOffset to p)

                        if (firstPoint) {
                            chartPath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                            firstPoint = false
                        } else {
                            chartPath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }

                    fillPath.lineTo(w, h)
                    fillPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw Main Chart Line
                    drawPath(
                        path = chartPath,
                        color = lineColor,
                        style = Stroke(width = 2.5f)
                    )

                    // Handle Touch Scrub / Inspection
                    val activeTouchX = touchX
                    if (activeTouchX != null && screenCoords.isNotEmpty()) {
                        val clampedX = activeTouchX.coerceIn(0f, w)
                        // Find closest point
                        val closest = screenCoords.minByOrNull { kotlin.math.abs(it.first.x - clampedX) }
                        if (closest != null) {
                            // Draw vertical inspection cursor
                            drawLine(
                                color = AeroCyan.copy(alpha = 0.8f),
                                start = Offset(closest.first.x, 0f),
                                end = Offset(closest.first.x, h),
                                strokeWidth = 1.5f
                            )

                            // Highlight point circle
                            drawCircle(
                                color = lineColor,
                                radius = 5f,
                                center = closest.first
                            )
                            drawCircle(
                                color = AeroBgMain,
                                radius = 2.5f,
                                center = closest.first
                            )

                            inspectedPoint = closest.second
                        }
                    }
                }
            }

            // Min and Max Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "MIN: ${String.format(Locale.US, "%.1f", minVal)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = AeroTextMuted,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "MAX: ${String.format(Locale.US, "%.1f", maxVal)} $unit",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = AeroTextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
