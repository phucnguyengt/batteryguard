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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricMeter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
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
import com.example.model.BatteryInfo
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun MetricsGrid(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val tempColor = when {
        batteryInfo.temperature >= 45f -> CyberRed
        batteryInfo.temperature >= 40f -> CyberAmber
        else -> NeonEmerald
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Row 1: Temperature & Voltage
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.DeviceThermostat,
                iconColor = tempColor,
                title = "Nhiệt độ Pin",
                value = "${String.format("%.1f", batteryInfo.temperature)} °C",
                subtitle = if (batteryInfo.temperature >= 42f) "Cảnh báo nóng" else "Mức tối ưu",
                modifier = Modifier.weight(1f).testTag("metric_temp_card")
            )
            MetricCard(
                icon = Icons.Default.ElectricMeter,
                iconColor = ElectricCyan,
                title = "Điện áp (V)",
                value = "${String.format("%.3f", batteryInfo.voltage)} V",
                subtitle = "Cell Voltage",
                modifier = Modifier.weight(1f).testTag("metric_voltage_card")
            )
        }

        // Row 2: Current & Power
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val currentPrefix = if (batteryInfo.currentMa > 0) "+" else ""
            MetricCard(
                icon = Icons.Default.Speed,
                iconColor = if (batteryInfo.currentMa >= 0) NeonEmerald else CyberAmber,
                title = "Dòng sạc / xả",
                value = "$currentPrefix${batteryInfo.currentMa} mA",
                subtitle = if (batteryInfo.isBypassActive) "Bypass Idle" else if (batteryInfo.currentMa > 0) "Dòng vào" else "Dòng xả",
                modifier = Modifier.weight(1f).testTag("metric_current_card")
            )
            MetricCard(
                icon = Icons.Default.FlashOn,
                iconColor = ElectricCyan,
                title = "Công suất",
                value = "${String.format("%.2f", batteryInfo.powerWatts)} W",
                subtitle = "Real-time Wattage",
                modifier = Modifier.weight(1f).testTag("metric_power_card")
            )
        }

        // Row 3: Health & Root Kernel Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                icon = Icons.Default.Cable,
                iconColor = if (batteryInfo.isPlugged) NeonEmerald else TextMuted,
                title = "Nguồn sạc",
                value = if (batteryInfo.isPlugged) "Cáp ${batteryInfo.plugType.name}" else "Chưa cắm",
                subtitle = "Sức khỏe: ${batteryInfo.health}",
                modifier = Modifier.weight(1f).testTag("metric_plug_card")
            )
            MetricCard(
                icon = Icons.Default.Security,
                iconColor = if (batteryInfo.isRootGranted) NeonEmerald else CyberAmber,
                title = "Quyền Root",
                value = if (batteryInfo.isRootGranted) "Đã cấp Root" else "Chưa cấp / Demo",
                subtitle = if (batteryInfo.isRootGranted) "libsu: Active" else "Chế độ kiểm thử",
                modifier = Modifier.weight(1f).testTag("metric_root_card")
            )
        }
    }
}

@Composable
fun MetricCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}
