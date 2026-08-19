package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryInfo
import com.example.model.BatteryStatus
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricCyanGlow
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldGlow
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechObsidian
import com.example.ui.theme.TechSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun BatteryGauge(
    batteryInfo: BatteryInfo,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = batteryInfo.level / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "BatteryLevelAnim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "GlowTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val isBypass = batteryInfo.isBypassActive || batteryInfo.status == BatteryStatus.NOT_CHARGING_BYPASS
    val isCharging = batteryInfo.status == BatteryStatus.CHARGING

    val activeColor = when {
        isBypass -> ElectricCyan
        isCharging -> NeonEmerald
        batteryInfo.level <= 20 -> CyberRed
        batteryInfo.level <= 40 -> CyberAmber
        else -> NeonEmerald
    }

    val gradientBrush = when {
        isBypass -> Brush.sweepGradient(listOf(ElectricCyan, Color(0xFF38BDF8), ElectricCyan))
        isCharging -> Brush.sweepGradient(listOf(NeonEmerald, ElectricCyan, NeonEmerald))
        batteryInfo.level <= 20 -> Brush.sweepGradient(listOf(CyberRed, CyberAmber, CyberRed))
        else -> Brush.sweepGradient(listOf(NeonEmeraldGlow, NeonEmerald, ElectricCyan))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular Gauge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(240.dp)
                .testTag("battery_gauge_circle")
        ) {
            // Background Canvas Arc & Glow
            Canvas(modifier = Modifier.size(230.dp)) {
                val strokeWidth = 14.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                val arcSize = Size(radius * 2, radius * 2)

                // Background track
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active progress arc
                val sweep = 270f * animatedProgress
                if (sweep > 0f) {
                    drawArc(
                        brush = gradientBrush,
                        startAngle = 135f,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Decorative tick marks around gauge
                val centerOffset = Offset(size.width / 2f, size.height / 2f)
                val tickRadius = radius - 16.dp.toPx()
            }

            // Inner Center Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Mini Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(activeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = when {
                            isBypass -> Icons.Default.Power
                            isCharging -> Icons.Default.Bolt
                            else -> Icons.Default.Shield
                        },
                        contentDescription = "Power Mode",
                        tint = activeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when {
                            isBypass -> "BYPASS USB"
                            isCharging -> "SẠC PIN"
                            else -> "PIN NỘI BỘ"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Battery Level Big Percentage
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${batteryInfo.level}",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "%",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                    )
                }

                // Live Power/Current Subtitle
                val currentText = when {
                    isBypass -> "0 mA (Nguồn USB)"
                    isCharging -> "+${batteryInfo.currentMa} mA • ${String.format("%.1f", batteryInfo.powerWatts)}W"
                    else -> "${batteryInfo.currentMa} mA • ${String.format("%.1f", batteryInfo.powerWatts)}W"
                }

                Text(
                    text = currentText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Badge Pill
        StatusPill(batteryInfo = batteryInfo)
    }
}

@Composable
fun StatusPill(batteryInfo: BatteryInfo) {
    val isBypass = batteryInfo.isBypassActive || batteryInfo.status == BatteryStatus.NOT_CHARGING_BYPASS
    val isCharging = batteryInfo.status == BatteryStatus.CHARGING

    val (bgColor, borderColor, textColor, title, sub) = when {
        isBypass -> Tuple5(
            Color(0xFF0C2738),
            ElectricCyan,
            ElectricCyan,
            "🛑 BYPASS CHARGING ACTIVE",
            "Đang dùng nguồn trực tiếp từ củ sạc • Không nạp vào pin"
        )
        isCharging -> Tuple5(
            Color(0xFF06331A),
            NeonEmerald,
            NeonEmerald,
            "⚡ ĐANG SẠC PIN NHANH",
            if (batteryInfo.activeLimitMa > 0) "Giới hạn dòng: ${batteryInfo.activeLimitMa}mA" else "Dòng sạc tối đa tiêu chuẩn"
        )
        batteryInfo.status == BatteryStatus.FULL -> Tuple5(
            Color(0xFF1E293B),
            NeonEmerald,
            NeonEmerald,
            "🔋 PIN ĐẦY 100%",
            "Khuyên dùng chế độ Bypass để tránh chai pin"
        )
        else -> Tuple5(
            Color(0xFF1E293B),
            TechBorder,
            TextSecondary,
            "🔋 ĐANG DÙNG NGUỒN PIN",
            "Chưa cắm cáp sạc USB/Type-C"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("status_pill_card")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(borderColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        isBypass -> Icons.Default.Power
                        isCharging -> Icons.Default.Bolt
                        else -> Icons.Default.Shield
                    },
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = sub,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
