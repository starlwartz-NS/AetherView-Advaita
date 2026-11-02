package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DesktopSidebar
import com.example.ui.components.DesktopTopHeader
import com.example.ui.screens.CommunicationScreen
import com.example.ui.screens.FlightAnalyticsScreen
import com.example.ui.screens.LiveTelemetryScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.MissionLogsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SystemSettingsScreen
import com.example.ui.theme.AeroBgMain
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MissionControlViewModel
import com.example.viewmodel.NavigationDestination

import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AeroBgCardElevated
import com.example.ui.theme.AeroCyan

enum class GroundStationUiMode {
    DESKTOP_WEB,
    NATIVE_COMPOSE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DesktopMissionControlApp()
            }
        }
    }
}

/**
 * Desktop-First Ground Station Application for Windows Laptops and Workstations.
 * Full-width desktop dashboard with fixed 240px left sidebar and wide multi-column views.
 */
@Composable
fun DesktopMissionControlApp(
    viewModel: MissionControlViewModel = viewModel()
) {
    var uiMode by remember { mutableStateOf(GroundStationUiMode.DESKTOP_WEB) }
    val currentDestination by viewModel.currentDestination.collectAsState()
    val config by viewModel.config.collectAsState()
    val latestPacket by viewModel.latestPacket.collectAsState()
    val history by viewModel.telemetryHistory.collectAsState()
    val recentPackets by viewModel.recentPackets.collectAsState()
    val systemHealth by viewModel.systemHealth.collectAsState()
    val flightAnalytics by viewModel.flightAnalytics.collectAsState()
    val missionEvents by viewModel.missionEvents.collectAsState()
    val filteredLogs by viewModel.filteredLogs.collectAsState()
    val searchQuery by viewModel.logSearchQuery.collectAsState()
    val phaseFilter by viewModel.logPhaseFilter.collectAsState()
    val isMonitoringPaused by viewModel.isMonitoringPaused.collectAsState()
    val chartTimeWindowSec by viewModel.chartTimeWindowSec.collectAsState()
    val isSimulationRunning by viewModel.isSimulationRunning.collectAsState()
    val isSimulationPaused by viewModel.isSimulationPaused.collectAsState()

    BackHandler(enabled = currentDestination != NavigationDestination.OVERVIEW) {
        viewModel.navigateTo(NavigationDestination.OVERVIEW)
    }

    val previousPacket = if (history.size >= 2) history[history.size - 2] else null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AeroBgMain
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(AeroBgMain)
        ) {
            when (uiMode) {
                GroundStationUiMode.DESKTOP_WEB -> {
                    // 100vw x 100vh Desktop Web Ground Station (Pure HTML5/CSS Grid/JS)
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true
                                settings.useWideViewPort = true
                                settings.loadWithOverviewMode = true
                                webViewClient = WebViewClient()
                                loadUrl("file:///android_asset/web/index.html")
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                GroundStationUiMode.NATIVE_COMPOSE -> {
                    // Full-width Desktop Ground Station built in Jetpack Compose
                    Row(modifier = Modifier.fillMaxSize()) {
                        // FIXED 240px LEFT SIDEBAR (Desktop Ground Station)
                        DesktopSidebar(
                            currentDestination = currentDestination,
                            onNavigate = { dest -> viewModel.navigateTo(dest) },
                            config = config,
                            systemHealth = systemHealth
                        )

                        // MAIN CONTENT AREA
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            DesktopTopHeader(
                                operatingMode = config.operatingMode,
                                latestPacket = latestPacket,
                                flightAnalytics = flightAnalytics,
                                isSimulationRunning = isSimulationRunning,
                                isSimulationPaused = isSimulationPaused,
                                onStartSimulation = { viewModel.startDemoMission() },
                                onPauseSimulation = { viewModel.pauseSimulation() },
                                onResumeSimulation = { viewModel.resumeSimulation() },
                                onResetSimulation = { viewModel.resetMission() }
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(AeroBgMain)
                            ) {
                                when (currentDestination) {
                                    NavigationDestination.OVERVIEW -> OverviewScreen(
                                        latestPacket = latestPacket,
                                        previousPacket = previousPacket,
                                        systemHealth = systemHealth,
                                        flightAnalytics = flightAnalytics,
                                        history = history,
                                        events = missionEvents,
                                        config = config
                                    )
                                    NavigationDestination.TELEMETRY -> LiveTelemetryScreen(
                                        history = history,
                                        isMonitoringPaused = isMonitoringPaused,
                                        timeWindowSec = chartTimeWindowSec,
                                        onTogglePause = { viewModel.toggleMonitoringPause() },
                                        onSelectTimeWindow = { win -> viewModel.setChartTimeWindow(win) }
                                    )
                                    NavigationDestination.ANALYTICS -> FlightAnalyticsScreen(
                                        analytics = flightAnalytics,
                                        history = history,
                                        events = missionEvents
                                    )
                                    NavigationDestination.TRACKING -> LiveTrackingScreen(
                                        latestPacket = latestPacket,
                                        history = history,
                                        config = config
                                    )
                                    NavigationDestination.COMMUNICATION -> CommunicationScreen(
                                        latestPacket = latestPacket,
                                        recentPackets = recentPackets,
                                        flightAnalytics = flightAnalytics,
                                        config = config
                                    )
                                    NavigationDestination.LOGS -> MissionLogsScreen(
                                        logs = filteredLogs,
                                        searchQuery = searchQuery,
                                        phaseFilter = phaseFilter,
                                        onSearchChanged = { query -> viewModel.setLogSearchQuery(query) },
                                        onPhaseFilterSelected = { phase -> viewModel.setLogPhaseFilter(phase) },
                                        onExportCsv = { ctx -> viewModel.exportCsv(ctx) }
                                    )
                                    NavigationDestination.SETTINGS -> SystemSettingsScreen(
                                        config = config,
                                        onUpdateConfig = { newConfig -> viewModel.updateConfig(newConfig) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Mode Switcher
            OutlinedButton(
                onClick = {
                    uiMode = if (uiMode == GroundStationUiMode.DESKTOP_WEB) {
                        GroundStationUiMode.NATIVE_COMPOSE
                    } else {
                        GroundStationUiMode.DESKTOP_WEB
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = AeroBgCardElevated.copy(alpha = 0.85f),
                    contentColor = AeroCyan
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
            ) {
                Text(
                    text = if (uiMode == GroundStationUiMode.DESKTOP_WEB) "⚡ Switch to Native Compose" else "🌐 Switch to Desktop Web App",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
