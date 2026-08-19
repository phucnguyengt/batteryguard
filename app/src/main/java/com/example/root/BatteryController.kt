package com.example.root

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import com.example.model.BatteryInfo
import com.example.model.BatteryStatus
import com.example.model.BypassMethod
import com.example.model.PlugType
import kotlin.math.abs

object BatteryController {

    // Common Sysfs Node Paths
    const val NODE_BATTERY_CAPACITY = "/sys/class/power_supply/battery/capacity"
    const val NODE_BATTERY_TEMP = "/sys/class/power_supply/battery/temp"
    const val NODE_BATTERY_VOLTAGE = "/sys/class/power_supply/battery/voltage_now"
    const val NODE_BATTERY_CURRENT = "/sys/class/power_supply/battery/current_now"
    const val NODE_INPUT_SUSPEND = "/sys/class/power_supply/battery/input_suspend"
    const val NODE_MAIN_CURRENT_MAX = "/sys/class/power_supply/main/constant_charge_current_max"
    const val NODE_BATTERY_CURRENT_MAX = "/sys/class/power_supply/battery/constant_charge_current_max"
    const val NODE_CHARGING_ENABLED = "/sys/class/power_supply/battery/charging_enabled"
    const val NODE_BATTERY_CHARGING_ENABLED = "/sys/class/power_supply/battery/battery_charging_enabled"
    const val NODE_CHARGE_CONTROL_LIMIT = "/sys/class/power_supply/battery/charge_control_limit"

    private var cachedIsRoot: Boolean? = null
    private var detectedNodes: List<String> = emptyList()

    suspend fun inspectKernelNodes(): List<String> {
        val candidates = listOf(
            NODE_BATTERY_CAPACITY,
            NODE_BATTERY_TEMP,
            NODE_BATTERY_VOLTAGE,
            NODE_BATTERY_CURRENT,
            NODE_INPUT_SUSPEND,
            NODE_MAIN_CURRENT_MAX,
            NODE_BATTERY_CURRENT_MAX,
            NODE_CHARGING_ENABLED,
            NODE_BATTERY_CHARGING_ENABLED,
            NODE_CHARGE_CONTROL_LIMIT,
            "/sys/class/power_supply/usb/type",
            "/sys/class/power_supply/battery/status"
        )
        val available = mutableListOf<String>()
        for (node in candidates) {
            if (RootHelper.checkNodeExists(node)) {
                available.add(node)
            }
        }
        detectedNodes = available
        return available
    }

    suspend fun readBatteryInfo(context: Context, isBypassActive: Boolean, activeLimitMa: Int): BatteryInfo {
        val isRoot = cachedIsRoot ?: RootHelper.isRootAvailable().also { cachedIsRoot = it }

        // 1. Android Standard BatteryManager
        var level = 50
        var scale = 100
        var temperature = 30.0f
        var voltage = 4.0f
        var isPlugged = false
        var plugType = PlugType.NONE
        var rawStatus = BatteryManager.BATTERY_STATUS_UNKNOWN
        var health = "Tốt (Good)"
        var technology = "Li-ion"
        var currentMa = 0

        try {
            val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatusIntent = context.registerReceiver(null, ifilter)
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

            val intentLevel = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            if (intentLevel >= 0) {
                level = intentLevel
            } else if (bm != null) {
                val bmCapacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                if (bmCapacity in 0..100) {
                    level = bmCapacity
                }
            }

            val tempRaw = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 300) ?: 300
            temperature = tempRaw / 10.0f

            val voltRaw = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4000) ?: 4000
            voltage = if (voltRaw > 100) voltRaw / 1000.0f else voltRaw.toFloat()

            val pluggedStatus = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
            isPlugged = pluggedStatus != 0
            plugType = when (pluggedStatus) {
                BatteryManager.BATTERY_PLUGGED_AC -> PlugType.AC
                BatteryManager.BATTERY_PLUGGED_USB -> PlugType.USB
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> PlugType.WIRELESS
                else -> if (isPlugged) PlugType.USB else PlugType.NONE
            }

            rawStatus = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val healthCode = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD) ?: BatteryManager.BATTERY_HEALTH_GOOD
            health = when (healthCode) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Tốt (Good)"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Quá nhiệt (Overheat)"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Hỏng (Dead)"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Quá áp (Over Voltage)"
                else -> "Bình thường"
            }
            technology = batteryStatusIntent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

            if (bm != null) {
                val currentNowMicro = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                if (currentNowMicro != Int.MIN_VALUE && currentNowMicro != 0) {
                    currentMa = if (abs(currentNowMicro) > 10000) currentNowMicro / 1000 else currentNowMicro
                }
            }
        } catch (e: Exception) {
            // Fallback gracefully
        }

        // 2. Sysfs Kernel Read Override if Root is available
        if (isRoot) {
            val sysCapacity = RootHelper.readSysfs(NODE_BATTERY_CAPACITY)?.toIntOrNull()
            if (sysCapacity != null && sysCapacity in 0..100) {
                level = sysCapacity
            }

            val sysTemp = RootHelper.readSysfs(NODE_BATTERY_TEMP)?.toFloatOrNull()
            if (sysTemp != null) {
                temperature = if (sysTemp > 150) sysTemp / 10.0f else sysTemp
            }

            val sysVolt = RootHelper.readSysfs(NODE_BATTERY_VOLTAGE)?.toFloatOrNull()
            if (sysVolt != null) {
                voltage = if (sysVolt > 100000) sysVolt / 1000000.0f else (if (sysVolt > 100) sysVolt / 1000.0f else sysVolt)
            }

            val sysCurr = RootHelper.readSysfs(NODE_BATTERY_CURRENT)?.toIntOrNull()
            if (sysCurr != null) {
                currentMa = if (abs(sysCurr) > 10000) sysCurr / 1000 else sysCurr
            }
        }

        // Determine Status
        val status = when {
            isBypassActive -> BatteryStatus.NOT_CHARGING_BYPASS
            rawStatus == BatteryManager.BATTERY_STATUS_FULL -> BatteryStatus.FULL
            rawStatus == BatteryManager.BATTERY_STATUS_CHARGING || (isPlugged && currentMa > 50) -> BatteryStatus.CHARGING
            rawStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING -> BatteryStatus.NOT_CHARGING_BYPASS
            isPlugged && abs(currentMa) <= 50 -> BatteryStatus.NOT_CHARGING_BYPASS
            else -> BatteryStatus.DISCHARGING
        }

        val powerWatts = (voltage * abs(currentMa)) / 1000.0f

        return BatteryInfo(
            level = level.coerceIn(0, 100),
            scale = scale,
            temperature = temperature,
            voltage = voltage,
            currentMa = currentMa,
            powerWatts = powerWatts,
            status = status,
            isPlugged = isPlugged,
            plugType = plugType,
            health = health,
            technology = technology,
            isBypassActive = isBypassActive,
            activeLimitMa = activeLimitMa,
            isRootGranted = isRoot,
            detectedKernelNodes = detectedNodes,
            lastUpdated = System.currentTimeMillis()
        )
    }

    suspend fun enableBypass(method: BypassMethod): CommandResult {
        return when (method) {
            BypassMethod.ZERO_CURRENT -> {
                // Qualcomm / Snapdragon Bypass: Zero Constant Current Max
                RootHelper.runRootCommand(
                    "echo 0 > $NODE_INPUT_SUSPEND 2>/dev/null || true",
                    "chmod 666 $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
                    "echo 0 > $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
                    "chmod 666 $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
                    "echo 0 > $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
                    "echo 0 > /sys/class/power_supply/battery/current_max 2>/dev/null || true"
                )
            }
            BypassMethod.INPUT_SUSPEND -> {
                // Hardware Suspend: Disconnect battery input
                RootHelper.runRootCommand(
                    "chmod 666 $NODE_INPUT_SUSPEND 2>/dev/null || true",
                    "echo 1 > $NODE_INPUT_SUSPEND"
                )
            }
            BypassMethod.CHARGING_DISABLE -> {
                // Charging Disable Node
                RootHelper.runRootCommand(
                    "chmod 666 $NODE_CHARGING_ENABLED 2>/dev/null || true",
                    "echo 0 > $NODE_CHARGING_ENABLED 2>/dev/null || true",
                    "chmod 666 $NODE_BATTERY_CHARGING_ENABLED 2>/dev/null || true",
                    "echo 0 > $NODE_BATTERY_CHARGING_ENABLED 2>/dev/null || true",
                    "echo 0 > $NODE_CHARGE_CONTROL_LIMIT 2>/dev/null || true"
                )
            }
        }
    }

    suspend fun disableBypass(): CommandResult {
        return RootHelper.runRootCommand(
            "echo 0 > $NODE_INPUT_SUSPEND 2>/dev/null || true",
            "chmod 666 $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
            "echo 3000000 > $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
            "chmod 666 $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
            "echo 3000000 > $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
            "chmod 666 $NODE_CHARGING_ENABLED 2>/dev/null || true",
            "echo 1 > $NODE_CHARGING_ENABLED 2>/dev/null || true",
            "chmod 666 $NODE_BATTERY_CHARGING_ENABLED 2>/dev/null || true",
            "echo 1 > $NODE_BATTERY_CHARGING_ENABLED 2>/dev/null || true",
            "killall -CONT hvdcp_opti mi_thermald thermanager 2>/dev/null || true"
        )
    }

    suspend fun setChargeCurrentLimit(targetMa: Int, freezeThermals: Boolean): CommandResult {
        if (targetMa <= 0) {
            return disableBypass()
        }
        val targetUa = targetMa * 1000

        val commands = mutableListOf<String>()
        if (freezeThermals) {
            commands.add("killall -STOP hvdcp_opti mi_thermald thermanager 2>/dev/null || true")
        }
        commands.addAll(
            listOf(
                "echo 0 > $NODE_INPUT_SUSPEND 2>/dev/null || true",
                "echo 1 > $NODE_CHARGING_ENABLED 2>/dev/null || true",
                "echo 1 > $NODE_BATTERY_CHARGING_ENABLED 2>/dev/null || true",
                "chmod 666 $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
                "echo $targetUa > $NODE_MAIN_CURRENT_MAX 2>/dev/null || true",
                "chmod 666 $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
                "echo $targetUa > $NODE_BATTERY_CURRENT_MAX 2>/dev/null || true",
                "echo $targetUa > /sys/class/power_supply/battery/current_max 2>/dev/null || true"
            )
        )
        return RootHelper.runRootCommand(*commands.toTypedArray())
    }

    suspend fun getDiagnostics(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        val nodes = listOf(
            NODE_BATTERY_CAPACITY,
            NODE_BATTERY_TEMP,
            NODE_BATTERY_VOLTAGE,
            NODE_BATTERY_CURRENT,
            NODE_INPUT_SUSPEND,
            NODE_MAIN_CURRENT_MAX,
            NODE_BATTERY_CURRENT_MAX,
            NODE_CHARGING_ENABLED,
            NODE_BATTERY_CHARGING_ENABLED,
            NODE_CHARGE_CONTROL_LIMIT,
            "/sys/class/power_supply/battery/status",
            "/sys/class/power_supply/usb/online"
        )
        for (node in nodes) {
            val value = RootHelper.readSysfs(node)
            map[node] = value ?: "Not Found / Inaccessible"
        }
        return map
    }
}
