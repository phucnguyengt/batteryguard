package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.ChargeLogEntity
import com.example.data.SettingsRepository
import com.example.service.BatteryGuardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsRepo = SettingsRepository(context.applicationContext)
                    val settings = settingsRepo.settingsFlow.first()

                    if (settings.autoStartOnBoot) {
                        val db = AppDatabase.getInstance(context.applicationContext)
                        db.chargeLogDao().insertLog(
                            ChargeLogEntity(
                                tag = "BOOT",
                                message = "Hệ thống khởi động. Tự động kích hoạt Battery Guard Pro Service.",
                                batteryLevel = 0,
                                temperature = 0f,
                                currentMa = 0,
                                voltage = 0f,
                                isBypassActive = false
                            )
                        )

                        val serviceIntent = Intent(context, BatteryGuardService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent)
                        } else {
                            context.startService(serviceIntent)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
