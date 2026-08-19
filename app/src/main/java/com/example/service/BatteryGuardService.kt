package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.data.ChargeLogEntity
import com.example.data.SettingsRepository
import com.example.model.BatteryInfo
import com.example.model.BatteryStatus
import com.example.model.PowerMode
import com.example.receiver.NotificationActionReceiver
import com.example.root.BatteryController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryGuardService : Service() {

    companion object {
        private const val TAG = "BatteryGuardService"
        const val CHANNEL_ID = "battery_guard_monitor_channel"
        const val NOTIFICATION_ID = 1001

        private val _liveBatteryInfo = MutableStateFlow(BatteryInfo())
        val liveBatteryInfo: StateFlow<BatteryInfo> = _liveBatteryInfo.asStateFlow()

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun requestModeUpdate(context: Context, mode: PowerMode) {
            val intent = Intent(context, BatteryGuardService::class.java).apply {
                putExtra("FORCE_MODE", mode.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var monitorJob: Job? = null
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var database: AppDatabase

    private var currentBypassState = false
    private var lastRecordedState: BatteryStatus? = null
    private var lastWarningTimestamp = 0L

    override fun onCreate() {
        super.onCreate()
        settingsRepo = SettingsRepository(applicationContext)
        database = AppDatabase.getInstance(applicationContext)
        createNotificationChannel()
        _isServiceRunning.value = true

        serviceScope.launch {
            settingsRepo.updateServiceRunning(true)
            BatteryController.inspectKernelNodes()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val forceModeStr = intent?.getStringExtra("FORCE_MODE")
        if (forceModeStr != null) {
            val mode = runCatching { PowerMode.valueOf(forceModeStr) }.getOrNull()
            if (mode != null) {
                serviceScope.launch {
                    handleDirectModeChange(mode)
                }
            }
        }

        startForegroundWithNotification()
        startMonitoringLoop()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        monitorJob?.cancel()
        serviceScope.launch {
            settingsRepo.updateServiceRunning(false)
        }
        serviceScope.cancel()
        _isServiceRunning.value = false
    }

    private fun startForegroundWithNotification() {
        val notification = buildNotification(_liveBatteryInfo.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startMonitoringLoop() {
        if (monitorJob?.isActive == true) return

        monitorJob = serviceScope.launch {
            while (isActive) {
                try {
                    val settings = settingsRepo.settingsFlow.first()
                    val batteryInfo = BatteryController.readBatteryInfo(
                        context = applicationContext,
                        isBypassActive = currentBypassState,
                        activeLimitMa = settings.chargeLimitMa
                    )

                    _liveBatteryInfo.value = batteryInfo

                    // Process Auto / Manual Threshold Logic
                    processBatteryLogic(batteryInfo, settings)

                    // Update sticky notification
                    if (settings.showStickyNotification) {
                        val notification = buildNotification(batteryInfo)
                        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        manager.notify(NOTIFICATION_ID, notification)
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "Error in monitor loop: ${e.message}")
                }
                delay(3000) // Update every 3 seconds as requested
            }
        }
    }

    private suspend fun handleDirectModeChange(mode: PowerMode) {
        val settings = settingsRepo.settingsFlow.first()
        when (mode) {
            PowerMode.FORCE_BYPASS -> {
                BatteryController.enableBypass(settings.bypassMethod)
                currentBypassState = true
                logEvent("BYPASS", "Kích hoạt Chế độ Nguồn trực tiếp (Force Bypass)", _liveBatteryInfo.value)
            }
            PowerMode.FORCE_CHARGE -> {
                if (settings.chargeLimitMa > 0) {
                    BatteryController.setChargeCurrentLimit(settings.chargeLimitMa, settings.freezeThermalDaemons)
                    logEvent("CHARGING", "Kích hoạt Sạc chậm giới hạn ${settings.chargeLimitMa}mA", _liveBatteryInfo.value)
                } else {
                    BatteryController.disableBypass()
                    logEvent("CHARGING", "Khôi phục sạc tiêu chuẩn tối đa", _liveBatteryInfo.value)
                }
                currentBypassState = false
            }
            PowerMode.AUTO -> {
                logEvent("AUTO", "Chuyển sang Chế độ Tự động bảo vệ theo ngưỡng", _liveBatteryInfo.value)
            }
        }
    }

    private suspend fun processBatteryLogic(info: BatteryInfo, settings: com.example.model.AppSettings) {
        // 1. Temperature Overheat Protection Check
        if (info.temperature >= settings.tempSafetyCutoff) {
            val now = System.currentTimeMillis()
            if (now - lastWarningTimestamp > 60000) { // Limit log to once per minute
                lastWarningTimestamp = now
                logEvent(
                    "WARNING",
                    "CẢNH BÁO: Nhiệt độ pin cao (${info.temperature}°C >= ${settings.tempSafetyCutoff}°C). Đang kích hoạt hạ nhiệt an toàn!",
                    info
                )
            }
        }

        // 2. Mode Handling
        when (settings.powerMode) {
            PowerMode.FORCE_BYPASS -> {
                if (!currentBypassState) {
                    BatteryController.enableBypass(settings.bypassMethod)
                    currentBypassState = true
                }
            }
            PowerMode.FORCE_CHARGE -> {
                if (currentBypassState) {
                    if (settings.chargeLimitMa > 0) {
                        BatteryController.setChargeCurrentLimit(settings.chargeLimitMa, settings.freezeThermalDaemons)
                    } else {
                        BatteryController.disableBypass()
                    }
                    currentBypassState = false
                }
            }
            PowerMode.AUTO -> {
                if (info.isPlugged) {
                    // Stop Level Reached -> Engage Bypass
                    if (info.level >= settings.stopLevel && !currentBypassState) {
                        BatteryController.enableBypass(settings.bypassMethod)
                        currentBypassState = true
                        logEvent(
                            "THRESHOLD_STOP",
                            "Mức pin ${info.level}% đã đạt ngưỡng dừng (${settings.stopLevel}%). Đã chuyển sang Nguồn trực tiếp USB (Bypass).",
                            info
                        )
                    }
                    // Resume Level Reached -> Resume Charging
                    else if (info.level <= settings.resumeLevel && currentBypassState) {
                        if (settings.chargeLimitMa > 0) {
                            BatteryController.setChargeCurrentLimit(settings.chargeLimitMa, settings.freezeThermalDaemons)
                            logEvent(
                                "THRESHOLD_RESUME",
                                "Mức pin ${info.level}% dưới ngưỡng tiếp tục (${settings.resumeLevel}%). Bật sạc lại ở mức ${settings.chargeLimitMa}mA.",
                                info
                            )
                        } else {
                            BatteryController.disableBypass()
                            logEvent(
                                "THRESHOLD_RESUME",
                                "Mức pin ${info.level}% dưới ngưỡng tiếp tục (${settings.resumeLevel}%). Bật sạc lại tiêu chuẩn.",
                                info
                            )
                        }
                        currentBypassState = false
                    }
                } else {
                    // Not plugged in -> reset bypass flag
                    if (currentBypassState) {
                        currentBypassState = false
                        BatteryController.disableBypass()
                    }
                }
            }
        }

        // Log status change transitions
        if (lastRecordedState != null && lastRecordedState != info.status) {
            val statusDesc = when (info.status) {
                BatteryStatus.NOT_CHARGING_BYPASS -> "Nguồn trực tiếp USB (Bypass Active)"
                BatteryStatus.CHARGING -> "Đang nạp sạc"
                BatteryStatus.DISCHARGING -> "Đang dùng pin"
                BatteryStatus.FULL -> "Pin đầy 100%"
                BatteryStatus.SUSPENDED -> "Ngắt sạc hệ thống"
                BatteryStatus.UNKNOWN -> "Không xác định"
            }
            logEvent("STATUS_CHANGE", "Trạng thái nguồn thay đổi: $statusDesc", info)
        }
        lastRecordedState = info.status
    }

    private suspend fun logEvent(tag: String, message: String, info: BatteryInfo) {
        try {
            database.chargeLogDao().insertLog(
                ChargeLogEntity(
                    tag = tag,
                    message = message,
                    batteryLevel = info.level,
                    temperature = info.temperature,
                    currentMa = info.currentMa,
                    voltage = info.voltage,
                    isBypassActive = currentBypassState
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert charge log: ${e.message}")
        }
    }

    private fun buildNotification(info: BatteryInfo): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action 1: Bypass
        val bypassIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TOGGLE_BYPASS
        }
        val bypassPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            bypassIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action 2: Auto
        val autoIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_TOGGLE_AUTO
        }
        val autoPendingIntent = PendingIntent.getBroadcast(
            this,
            2,
            autoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Action 3: Force Charge
        val chargeIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_FORCE_CHARGE
        }
        val chargePendingIntent = PendingIntent.getBroadcast(
            this,
            3,
            chargeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = when {
            currentBypassState -> "🛑 Nguồn trực tiếp (Bypass USB)"
            info.status == BatteryStatus.CHARGING -> "⚡ Đang sạc: +${info.currentMa}mA (${String.format("%.1f", info.powerWatts)}W)"
            info.status == BatteryStatus.NOT_CHARGING_BYPASS -> "🛑 Ngắt dòng pin (Bypass)"
            else -> "🔋 Dùng pin: ${info.currentMa}mA"
        }

        val contentText = "${info.level}% | ${String.format("%.1f", info.temperature)}°C | ${String.format("%.2f", info.voltage)}V | $statusText"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Battery Guard Pro — $statusText")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Mức pin: ${info.level}% • Nhiệt độ: ${String.format("%.1f", info.temperature)}°C • Điện áp: ${String.format("%.2f", info.voltage)}V • Dòng: ${info.currentMa}mA • Nguồn: $statusText"))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(0, "🛑 Bypass", bypassPendingIntent)
            .addAction(0, "⚡ Tự động", autoPendingIntent)
            .addAction(0, "▶️ Sạc lại", chargePendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Guard Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Hiển thị thông số pin, nhiệt độ và điều khiển sạc thời gian thực"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
