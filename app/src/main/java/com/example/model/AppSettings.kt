package com.example.model

enum class BypassMethod(val displayName: String, val description: String) {
    ZERO_CURRENT(
        displayName = "Zero Current Limit (Qualcomm/Snapdragon)",
        description = "Sets charge current max to 0 mA on battery and main nodes. Recommended for most devices."
    ),
    INPUT_SUSPEND(
        displayName = "Input Suspend Node",
        description = "Toggles input_suspend node (1=suspend, 0=resume). Fast & direct kernel switch."
    ),
    CHARGING_DISABLE(
        displayName = "Charging Control / Enabled Node",
        description = "Toggles charging_enabled or charge_control_limit sysfs parameters."
    )
}

enum class PowerMode {
    AUTO,
    FORCE_BYPASS,
    FORCE_CHARGE
}

data class AppSettings(
    val isAutoModeEnabled: Boolean = true,
    val powerMode: PowerMode = PowerMode.AUTO,
    val stopLevel: Int = 80,
    val resumeLevel: Int = 30,
    val chargeLimitMa: Int = 0, // 0 = Max/Default, 500, 1000, 1500, 2000
    val bypassMethod: BypassMethod = BypassMethod.ZERO_CURRENT,
    val autoStartOnBoot: Boolean = true,
    val showStickyNotification: Boolean = true,
    val soundAlertOnThreshold: Boolean = false,
    val tempSafetyCutoff: Int = 45, // Alert or throttle if temp >= 45°C
    val freezeThermalDaemons: Boolean = false,
    val isServiceRunning: Boolean = false
)
