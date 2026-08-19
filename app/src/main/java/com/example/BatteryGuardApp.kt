package com.example

import android.app.Application
import android.content.Intent
import android.os.Build
import com.example.data.SettingsRepository
import com.example.service.BatteryGuardService
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BatteryGuardApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure libsu defaults globally
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )

        // Automatically launch background monitor service if enabled in settings
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsRepo = SettingsRepository(this@BatteryGuardApp)
                val settings = settingsRepo.settingsFlow.first()
                if (settings.autoStartOnBoot) {
                    val serviceIntent = Intent(this@BatteryGuardApp, BatteryGuardService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                }
            } catch (e: Exception) {
                // Ignore initialization errors
            }
        }
    }
}
