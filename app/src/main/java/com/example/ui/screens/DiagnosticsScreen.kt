package com.example.ui.screens

import android.os.Build
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.root.CommandResult
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
fun DiagnosticsScreen(
    diagnosticsMap: Map<String, String>,
    commandResult: CommandResult?,
    onRefresh: () -> Unit,
    onRunCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }

    val quickCommands = listOf(
        Pair("cat /sys/class/power_supply/battery/capacity", "Mức pin"),
        Pair("cat /sys/class/power_supply/battery/current_now", "Dòng mA"),
        Pair("cat /sys/class/power_supply/battery/temp", "Nhiệt độ"),
        Pair("cat /sys/class/power_supply/battery/input_suspend", "Input Suspend"),
        Pair("ls -la /sys/class/power_supply/", "Liệt kê Nodes")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TechObsidian)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Device Hardware Specs Card
            DeviceSpecsCard()
        }

        // Shell Terminal Sandbox Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(TechSurfaceCard)
                    .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
                    .testTag("terminal_card")
            ) {
                Column {
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
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Root Shell Terminal (libsu)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Thực thi lệnh kiểm tra Sysfs Kernel trực tiếp",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Command Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickCommands) { (cmd, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TechSurfaceElevated)
                                    .border(1.dp, TechBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        commandInput = cmd
                                        onRunCommand(cmd)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = ElectricCyan,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Command Input Box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            placeholder = { Text("Nhập lệnh root...", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = TextPrimary
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = TechBorder,
                                focusedContainerColor = TechSurfaceElevated,
                                unfocusedContainerColor = TechSurfaceElevated
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                if (commandInput.isNotBlank()) {
                                    onRunCommand(commandInput)
                                }
                            }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_root_command")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (commandInput.isNotBlank()) {
                                    onRunCommand(commandInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_run_command")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = TechObsidian,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Terminal Output Window
                    if (commandResult != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF030712))
                                .border(1.dp, TechBorder, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "OUTPUT (Mã lỗi: ${if (commandResult.isSuccess) "0" else "1"})",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (commandResult.isSuccess) NeonEmerald else CyberRed
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                val outputText = if (commandResult.output.isNotEmpty()) {
                                    commandResult.output.joinToString("\n")
                                } else if (!commandResult.errorMessage.isNullOrBlank()) {
                                    "Error: ${commandResult.errorMessage}"
                                } else {
                                    "(No output returned - Command executed successfully)"
                                }

                                Text(
                                    text = outputText,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (commandResult.isSuccess) NeonEmerald else CyberRed
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Sysfs Node Inspector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(TechSurfaceCard)
                    .border(1.dp, TechBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NeonEmerald.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DataObject,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Sysfs Power Nodes Trực tiếp",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Giá trị kernel theo thời gian thực",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("btn_refresh_diagnostics")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = NeonEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (diagnosticsMap.isEmpty()) {
                        Text(
                            text = "Đang quét sysfs nodes...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            diagnosticsMap.forEach { (nodePath, value) ->
                                NodeItemRow(path = nodePath, value = value)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DeviceSpecsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(TechSurfaceCard)
            .border(1.dp, TechBorder, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                text = "THÔNG TIN THIẾT BỊ & NỀN TẢNG",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Thiết bị", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${Build.MANUFACTURER.capitalize()} ${Build.MODEL}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Column {
                    Text(text = "Chipset / SoC", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = Build.HARDWARE.capitalize(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Column {
                    Text(text = "Android OS", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonEmerald
                    )
                }
            }
        }
    }
}

@Composable
fun NodeItemRow(path: String, value: String) {
    val isPresent = !value.contains("Not Found", ignoreCase = true)
    val shortName = path.substringAfterLast("/")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TechSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = shortName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPresent) ElectricCyan else TextMuted
            )
            Text(
                text = path,
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isPresent) Color(0xFF0C2738) else Color(0xFF1E293B))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isPresent) NeonEmerald else TextMuted
            )
        }
    }
}
