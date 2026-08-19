package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.BatteryInfo
import com.example.model.PowerMode
import com.example.ui.components.BatteryGauge
import com.example.ui.components.MetricsGrid
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechObsidian
import com.example.ui.theme.TechSurfaceCard
import com.example.ui.theme.TechSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    batteryInfo: BatteryInfo,
    settings: AppSettings,
    isServiceRunning: Boolean,
    onSetPowerMode: (PowerMode) -> Unit,
    onSetCurrentLimit: (Int) -> Unit,
    onToggleService: (Boolean) -> Unit,
    onCheckRoot: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechObsidian)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Root Status Alert Card if needed
            RootStatusBanner(
                isRootGranted = batteryInfo.isRootGranted,
                detectedNodesCount = batteryInfo.detectedKernelNodes.size,
                onCheckRoot = onCheckRoot
            )
        }

        // Circular Gauge
        item {
            BatteryGauge(batteryInfo = batteryInfo)
        }

        // 3 Quick Power Mode Buttons (Auto, Bypass, Force Charge)
        item {
            PowerModeSelector(
                currentMode = settings.powerMode,
                onModeSelected = onSetPowerMode
            )
        }

        // Current Limit Quick Selector (Slow Charging Preset)
        item {
            CurrentLimitSelector(
                selectedLimit = settings.chargeLimitMa,
                onLimitSelected = onSetCurrentLimit
            )
        }

        // Foreground Service Status Card
        item {
            ServiceStatusCard(
                isServiceRunning = isServiceRunning,
                onToggleService = onToggleService,
                stopLevel = settings.stopLevel,
                resumeLevel = settings.resumeLevel,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // Realtime Detailed Metrics Grid
        item {
            Text(
                text = "THÔNG SỐ PHẦN CỨNG & NGUỒN",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
            MetricsGrid(batteryInfo = batteryInfo)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RootStatusBanner(
    isRootGranted: Boolean,
    detectedNodesCount: Int,
    onCheckRoot: () -> Unit
) {
    val bgColor = if (isRootGranted) Color(0xFF072719) else Color(0xFF2D1B05)
    val borderColor = if (isRootGranted) NeonEmerald else CyberAmber
    val textColor = if (isRootGranted) NeonEmerald else CyberAmber

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(12.dp)
            .testTag("root_status_banner")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isRootGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isRootGranted) "Quyền Root Kernel đã kích hoạt (libsu)" else "Chế độ Mô phỏng / Chưa cấp Root",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (isRootGranted) "Đã phát hiện $detectedNodesCount sysfs node sạc Qualcomm/Snapdragon" else "Cần cấp quyền Root để can thiệp ngắt nguồn qua Kernel",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onCheckRoot,
                modifier = Modifier.size(36.dp).testTag("btn_recheck_root")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recheck Root",
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun PowerModeSelector(
    currentMode: PowerMode,
    onModeSelected: (PowerMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "CHẾ ĐỘ QUẢN LÝ NGUỒN",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Mode 1: Auto Mode
            ModeButton(
                icon = Icons.Default.AutoAwesome,
                title = "Tự động",
                subtitle = "Bảo vệ theo ngưỡng",
                isActive = currentMode == PowerMode.AUTO,
                accentColor = NeonEmerald,
                onClick = { onModeSelected(PowerMode.AUTO) },
                modifier = Modifier.weight(1f).testTag("btn_mode_auto")
            )

            // Mode 2: Force Bypass
            ModeButton(
                icon = Icons.Default.PowerOff,
                title = "Bypass USB",
                subtitle = "Ngắt sạc tức thì",
                isActive = currentMode == PowerMode.FORCE_BYPASS,
                accentColor = ElectricCyan,
                onClick = { onModeSelected(PowerMode.FORCE_BYPASS) },
                modifier = Modifier.weight(1f).testTag("btn_mode_bypass")
            )

            // Mode 3: Force Charge
            ModeButton(
                icon = Icons.Default.PlayArrow,
                title = "Sạc ngay",
                subtitle = "Nạp pin liên tục",
                isActive = currentMode == PowerMode.FORCE_CHARGE,
                accentColor = CyberAmber,
                onClick = { onModeSelected(PowerMode.FORCE_CHARGE) },
                modifier = Modifier.weight(1f).testTag("btn_mode_charge")
            )
        }
    }
}

@Composable
fun ModeButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isActive) accentColor.copy(alpha = 0.15f) else TechSurfaceCard
    val border = if (isActive) accentColor else TechBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(if (isActive) 1.5.dp else 1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isActive) accentColor.copy(alpha = 0.25f) else TechSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isActive) accentColor else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) TextPrimary else TextSecondary
            )

            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isActive) accentColor else TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun CurrentLimitSelector(
    selectedLimit: Int,
    onLimitSelected: (Int) -> Unit
) {
    val presets = listOf(
        Pair(0, "Tối đa"),
        Pair(500, "500mA"),
        Pair(1000, "1000mA"),
        Pair(1500, "1500mA"),
        Pair(2000, "2000mA")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GIỚI HẠN DÒNG SẠC CHẬM",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Text(
                text = if (selectedLimit == 0) "Mặc định (Không giới hạn)" else "$selectedLimit mA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NeonEmerald
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { (value, label) ->
                val isSelected = selectedLimit == value
                val bg = if (isSelected) NeonEmerald.copy(alpha = 0.15f) else TechSurfaceCard
                val border = if (isSelected) NeonEmerald else TechBorder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(12.dp))
                        .clickable { onLimitSelected(value) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("chip_limit_$value")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = NeonEmerald,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) NeonEmerald else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceStatusCard(
    isServiceRunning: Boolean,
    onToggleService: (Boolean) -> Unit,
    stopLevel: Int,
    resumeLevel: Int,
    onNavigateToSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .testTag("service_status_card")
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isServiceRunning) NeonEmerald.copy(alpha = 0.15f) else TechBorder),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = if (isServiceRunning) NeonEmerald else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Dịch vụ giám sát ngầm (3s/lần)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isServiceRunning) "Đang chạy • Ngắt sạc @$stopLevel% / Nạp lại @$resumeLevel%" else "Đã tạm dừng giám sát",
                        fontSize = 11.sp,
                        color = if (isServiceRunning) NeonEmerald else TextMuted
                    )
                }

                Switch(
                    checked = isServiceRunning,
                    onCheckedChange = onToggleService,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonEmerald,
                        checkedTrackColor = NeonEmerald.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = TechBorder
                    ),
                    modifier = Modifier.testTag("switch_service_toggle")
                )
            }
        }
    }
}
