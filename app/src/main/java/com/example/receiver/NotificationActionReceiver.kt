package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.SettingsRepository
import com.example.model.PowerMode
import com.example.service.BatteryGuardService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TOGGLE_BYPASS = "com.example.batteryguard.ACTION_TOGGLE_BYPASS"
        const val ACTION_TOGGLE_AUTO = "com.example.batteryguard.ACTION_TOGGLE_AUTO"
        const val ACTION_FORCE_CHARGE = "com.example.batteryguard.ACTION_FORCE_CHARGE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val settingsRepo = SettingsRepository(context.applicationContext)
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_TOGGLE_BYPASS -> {
                        settingsRepo.updatePowerMode(PowerMode.FORCE_BYPASS)
                        BatteryGuardService.requestModeUpdate(context, PowerMode.FORCE_BYPASS)
                    }
                    ACTION_TOGGLE_AUTO -> {
                        settingsRepo.updatePowerMode(PowerMode.AUTO)
                        BatteryGuardService.requestModeUpdate(context, PowerMode.AUTO)
                    }
                    ACTION_FORCE_CHARGE -> {
                        settingsRepo.updatePowerMode(PowerMode.FORCE_CHARGE)
                        BatteryGuardService.requestModeUpdate(context, PowerMode.FORCE_CHARGE)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
