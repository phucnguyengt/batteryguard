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
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.TechBorder
import com.example.ui.theme.TechSurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ThresholdSliderCard(
    stopLevel: Int,
    resumeLevel: Int,
    onStopLevelChange: (Int) -> Unit,
    onResumeLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("threshold_slider_card")
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Ngưỡng sạc tự động (Auto Threshold)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tự động ngắt khi pin chạm đỉnh & sạc lại khi cạn",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Visual Band Bar Diagram
            VisualBandBar(
                resumeLevel = resumeLevel,
                stopLevel = stopLevel
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Stop Level Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ngưỡng ngắt sạc (Bypass)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "$stopLevel%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Slider(
                    value = stopLevel.toFloat(),
                    onValueChange = { onStopLevelChange(it.toInt()) },
                    valueRange = 50f..95f,
                    steps = 8, // 5% step intervals
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = TechBorder
                    ),
                    modifier = Modifier.testTag("slider_stop_level")
                )

                Text(
                    text = "Khi pin >= $stopLevel%, hệ thống sẽ ngắt sạc và chuyển máy sang chạy nguồn USB trực tiếp.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Resume Level Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = NeonEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ngưỡng tiếp tục sạc (Resume)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "$resumeLevel%",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                }

                Slider(
                    value = resumeLevel.toFloat(),
                    onValueChange = { onResumeLevelChange(it.toInt()) },
                    valueRange = 15f..60f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonEmerald,
                        activeTrackColor = NeonEmerald,
                        inactiveTrackColor = TechBorder
                    ),
                    modifier = Modifier.testTag("slider_resume_level")
                )

                Text(
                    text = "Khi pin <= $resumeLevel%, hệ thống sẽ tự động bật sạc lại pin theo dòng đã chọn.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun VisualBandBar(
    resumeLevel: Int,
    stopLevel: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(TechBorder)
        ) {
            // Band 1: Discharging / Under resume (0% -> resumeLevel)
            Box(
                modifier = Modifier
                    .weight(resumeLevel.coerceAtLeast(1).toFloat())
                    .height(14.dp)
                    .background(CyberAmber.copy(alpha = 0.6f))
            )

            // Band 2: Charging Range (resumeLevel -> stopLevel)
            Box(
                modifier = Modifier
                    .weight((stopLevel - resumeLevel).coerceAtLeast(1).toFloat())
                    .height(14.dp)
                    .background(NeonEmerald)
            )

            // Band 3: Protected Band (stopLevel -> 100%)
            Box(
                modifier = Modifier
                    .weight((100 - stopLevel).coerceAtLeast(1).toFloat())
                    .height(14.dp)
                    .background(ElectricCyan)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "0%", fontSize = 10.sp, color = TextMuted)
            Text(text = "Sạc lại: $resumeLevel%", fontSize = 10.sp, color = NeonEmerald, fontWeight = FontWeight.Bold)
            Text(text = "Bypass: $stopLevel%", fontSize = 10.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
            Text(text = "100%", fontSize = 10.sp, color = TextMuted)
        }
    }
}
