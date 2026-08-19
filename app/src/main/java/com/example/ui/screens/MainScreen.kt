package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BatteryInfo
import com.example.model.BatteryStatus
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechObsidian
import com.example.ui.theme.TechSurface
import com.example.ui.theme.TechSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BatteryViewModel

enum class MainTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Tổng quan", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SETTINGS("Cài đặt", Icons.Filled.Settings, Icons.Outlined.Settings),
    LOGS("Nhật ký", Icons.Filled.History, Icons.Outlined.History),
    DIAGNOSTICS("Chẩn đoán", Icons.Filled.Terminal, Icons.Outlined.Terminal)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BatteryViewModel,
    modifier: Modifier = Modifier
) {
    val batteryInfo by viewModel.liveBatteryInfo.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val isServiceRunning by viewModel.isServiceRunning.collectAsStateWithLifecycle()
    val diagnosticsMap by viewModel.diagnosticsMap.collectAsStateWithLifecycle()
    val commandResult by viewModel.commandResult.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Battery Guard Pro",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (batteryInfo.isBypassActive) ElectricCyan.copy(alpha = 0.2f) else NeonEmerald.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (batteryInfo.isBypassActive) Icons.Default.Power else Icons.Default.Speed,
                            contentDescription = null,
                            tint = if (batteryInfo.isBypassActive) ElectricCyan else NeonEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(CircleShape)
                            .background(if (batteryInfo.isRootGranted) NeonEmerald.copy(alpha = 0.15f) else Color(0xFF2D1B05))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (batteryInfo.isRootGranted) "ROOT" else "DEMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (batteryInfo.isRootGranted) NeonEmerald else Color(0xFFFFB300)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = TechObsidian
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = TechSurface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar")
            ) {
                MainTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ElectricCyan,
                            selectedTextColor = ElectricCyan,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = ElectricCyan.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        containerColor = TechObsidian,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> DashboardScreen(
                    batteryInfo = batteryInfo,
                    settings = settings,
                    isServiceRunning = isServiceRunning,
                    onSetPowerMode = viewModel::setPowerMode,
                    onSetCurrentLimit = viewModel::setChargeLimitPreset,
                    onToggleService = viewModel::toggleService,
                    onCheckRoot = viewModel::checkRootAndInspectNodes,
                    onNavigateToSettings = { selectedTabIndex = 1 }
                )

                1 -> SettingsScreen(
                    settings = settings,
                    onStopLevelChange = viewModel::setStopLevel,
                    onResumeLevelChange = viewModel::setResumeLevel,
                    onChargeLimitChange = viewModel::setChargeLimitPreset,
                    onBypassMethodChange = viewModel::setBypassMethod,
                    onAutoStartChange = viewModel::setAutoStartOnBoot,
                    onStickyNotificationChange = viewModel::setStickyNotification,
                    onFreezeThermalChange = viewModel::setFreezeThermalDaemons,
                    onTempCutoffChange = viewModel::setTempSafetyCutoff
                )

                2 -> LogsScreen(
                    logs = logs,
                    onClearLogs = viewModel::clearLogs,
                    onSimulateTestLog = viewModel::triggerTestSimulatedBypass
                )

                3 -> DiagnosticsScreen(
                    diagnosticsMap = diagnosticsMap,
                    commandResult = commandResult,
                    onRefresh = viewModel::refreshDiagnostics,
                    onRunCommand = viewModel::runCustomShellCommand
                )
            }
        }
    }
}
