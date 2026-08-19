package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.BypassMethod
import com.example.ui.components.ThresholdSliderCard
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
fun SettingsScreen(
    settings: AppSettings,
    onStopLevelChange: (Int) -> Unit,
    onResumeLevelChange: (Int) -> Unit,
    onChargeLimitChange: (Int) -> Unit,
    onBypassMethodChange: (BypassMethod) -> Unit,
    onAutoStartChange: (Boolean) -> Unit,
    onStickyNotificationChange: (Boolean) -> Unit,
    onFreezeThermalChange: (Boolean) -> Unit,
    onTempCutoffChange: (Int) -> Unit,
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
            Text(
                text = "CÀI ĐẶT BẢO VỆ PIN & ROOT",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 1. Dual Threshold Slider Card
        item {
            ThresholdSliderCard(
                stopLevel = settings.stopLevel,
                resumeLevel = settings.resumeLevel,
                onStopLevelChange = onStopLevelChange,
                onResumeLevelChange = onResumeLevelChange
            )
        }

        // 2. Bypass Kernel Method Selection Card
        item {
            BypassMethodSelectionCard(
                selectedMethod = settings.bypassMethod,
                onMethodSelect = onBypassMethodChange
            )
        }

        // 3. Slow Charging Limit Setting Card
        item {
            ChargeLimitSettingCard(
                selectedLimit = settings.chargeLimitMa,
                onLimitSelected = onChargeLimitChange
            )
        }

        // 4. Advanced Hardware & Thermal Tweaks
        item {
            AdvancedTweaksCard(
                freezeThermal = settings.freezeThermalDaemons,
                onFreezeThermalChange = onFreezeThermalChange,
                tempCutoff = settings.tempSafetyCutoff,
                onTempCutoffChange = onTempCutoffChange
            )
        }

        // 5. System & Notification Switches
        item {
            SystemOptionsCard(
                autoStart = settings.autoStartOnBoot,
                onAutoStartChange = onAutoStartChange,
                stickyNotification = settings.showStickyNotification,
                onStickyNotificationChange = onStickyNotificationChange
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BypassMethodSelectionCard(
    selectedMethod: BypassMethod,
    onMethodSelect: (BypassMethod) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("bypass_method_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Power,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Phương thức Kernel Bypass",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cơ chế ngắt dòng vào pin của kernel",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            BypassMethod.values().forEachIndexed { index, method ->
                val isSelected = method == selectedMethod
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.1f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onMethodSelect(method) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) ElectricCyan else TextMuted,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = method.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ElectricCyan else TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = method.description,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
                if (index < BypassMethod.values().size - 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun ChargeLimitSettingCard(
    selectedLimit: Int,
    onLimitSelected: (Int) -> Unit
) {
    val options = listOf(
        Pair(0, "Tối đa (Không giới hạn)"),
        Pair(500, "500 mA (Sạc siêu chậm - Giữ pin cực mát)"),
        Pair(1000, "1000 mA (1A - Phù hợp chơi game/treo máy)"),
        Pair(1500, "1500 mA (1.5A - Tối ưu hàng ngày)"),
        Pair(2000, "2000 mA (2A - Sạc nhanh an toàn)")
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("charge_limit_setting_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BatteryChargingFull,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Giới hạn dòng sạc (Slow Charging)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tránh sinh nhiệt khi cắm sạc thời gian dài",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            options.forEach { (limitVal, title) ->
                val isSelected = limitVal == selectedLimit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NeonEmerald.copy(alpha = 0.1f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) NeonEmerald.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onLimitSelected(limitVal) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isSelected) NeonEmerald else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) NeonEmerald else TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun AdvancedTweaksCard(
    freezeThermal: Boolean,
    onFreezeThermalChange: (Boolean) -> Unit,
    tempCutoff: Int,
    onTempCutoffChange: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("advanced_tweaks_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberAmber.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyberAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Tối ưu hóa nhiệt độ & Snapdragon",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Quản lý tiến trình nhiệt độc quyền",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch 1: Freeze thermal daemons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Đóng băng Quick Charge Daemon",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "killall -STOP hvdcp_opti mi_thermald để ngăn hệ thống tự động đẩy dòng sạc cao",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = freezeThermal,
                    onCheckedChange = onFreezeThermalChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberAmber,
                        checkedTrackColor = CyberAmber.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("switch_freeze_thermal")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = TechBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Temperature Cutoff Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ngưỡng cảnh báo quá nhiệt",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "$tempCutoff °C",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberRed
                    )
                }

                Slider(
                    value = tempCutoff.toFloat(),
                    onValueChange = { onTempCutoffChange(it.toInt()) },
                    valueRange = 38f..55f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberRed,
                        activeTrackColor = CyberRed,
                        inactiveTrackColor = TechBorder
                    ),
                    modifier = Modifier.testTag("slider_temp_cutoff")
                )

                Text(
                    text = "Khi pin chạm $tempCutoff°C, ứng dụng sẽ ghi nhận cảnh báo và tự động can thiệp giảm nhiệt.",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun SystemOptionsCard(
    autoStart: Boolean,
    onAutoStartChange: (Boolean) -> Unit,
    stickyNotification: Boolean,
    onStickyNotificationChange: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("system_options_card")
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(NeonEmerald.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = null,
                        tint = NeonEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Hệ thống & Thông báo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tự động kích hoạt khi mở máy",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Switch: Auto start on boot
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Khởi động cùng hệ thống (Boot Completed)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tự động chạy Foreground Service bảo vệ khi máy khởi động lại",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = autoStart,
                    onCheckedChange = onAutoStartChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonEmerald,
                        checkedTrackColor = NeonEmerald.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("switch_auto_start")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = TechBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Switch: Sticky Notification
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thông báo cố định trên thanh trạng thái",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Hiển thị % pin, nhiệt độ, dòng sạc trực tiếp và 3 nút gạt nhanh",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
                Switch(
                    checked = stickyNotification,
                    onCheckedChange = onStickyNotificationChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NeonEmerald,
                        checkedTrackColor = NeonEmerald.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.testTag("switch_sticky_notification")
                )
            }
        }
    }
}
