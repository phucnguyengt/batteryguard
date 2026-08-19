package com.example.model

enum class BatteryStatus {
    CHARGING,
    DISCHARGING,
    NOT_CHARGING_BYPASS,
    SUSPENDED,
    FULL,
    UNKNOWN
}

enum class PlugType {
    NONE,
    AC,
    USB,
    WIRELESS
}

data class BatteryInfo(
    val level: Int = 0,
    val scale: Int = 100,
    val temperature: Float = 0.0f,
    val voltage: Float = 0.0f,
    val currentMa: Int = 0,
    val powerWatts: Float = 0.0f,
    val status: BatteryStatus = BatteryStatus.UNKNOWN,
    val isPlugged: Boolean = false,
    val plugType: PlugType = PlugType.NONE,
    val health: String = "Good",
    val technology: String = "Li-ion",
    val isBypassActive: Boolean = false,
    val activeLimitMa: Int = 0,
    val isRootGranted: Boolean = false,
    val detectedKernelNodes: List<String> = emptyList(),
    val capacityMah: Int = 4500,
    val lastUpdated: Long = System.currentTimeMillis()
)
