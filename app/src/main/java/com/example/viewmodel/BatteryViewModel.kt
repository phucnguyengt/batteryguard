package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ChargeLogEntity
import com.example.data.SettingsRepository
import com.example.model.AppSettings
import com.example.model.BatteryInfo
import com.example.model.BypassMethod
import com.example.model.PowerMode
import com.example.root.BatteryController
import com.example.root.CommandResult
import com.example.root.RootHelper
import com.example.service.BatteryGuardService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val database = AppDatabase.getInstance(application)

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    val liveBatteryInfo: StateFlow<BatteryInfo> = BatteryGuardService.liveBatteryInfo

    val logs: StateFlow<List<ChargeLogEntity>> = database.chargeLogDao().getAllLogs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val isServiceRunning: StateFlow<Boolean> = BatteryGuardService.isServiceRunning

    private val _isRootChecking = MutableStateFlow(false)
    val isRootChecking: StateFlow<Boolean> = _isRootChecking.asStateFlow()

    private val _diagnosticsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val diagnosticsMap: StateFlow<Map<String, String>> = _diagnosticsMap.asStateFlow()

    private val _commandResult = MutableStateFlow<CommandResult?>(null)
    val commandResult: StateFlow<CommandResult?> = _commandResult.asStateFlow()

    init {
        checkRootAndInspectNodes()
        ensureServiceStarted()
    }

    fun checkRootAndInspectNodes() {
        viewModelScope.launch {
            _isRootChecking.value = true
            try {
                RootHelper.isRootAvailable()
                BatteryController.inspectKernelNodes()
                refreshDiagnostics()
            } finally {
                _isRootChecking.value = false
            }
        }
    }

    fun refreshDiagnostics() {
        viewModelScope.launch {
            _diagnosticsMap.value = BatteryController.getDiagnostics()
        }
    }

    fun toggleService(enable: Boolean) {
        val app = getApplication<Application>()
        if (enable) {
            val intent = Intent(app, BatteryGuardService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } else {
            val intent = Intent(app, BatteryGuardService::class.java)
            app.stopService(intent)
        }
    }

    fun setPowerMode(mode: PowerMode) {
        viewModelScope.launch {
            settingsRepo.updatePowerMode(mode)
            val app = getApplication<Application>()
            BatteryGuardService.requestModeUpdate(app, mode)
        }
    }

    fun setChargeLimitPreset(limitMa: Int) {
        viewModelScope.launch {
            settingsRepo.updateChargeLimitMa(limitMa)
            if (settings.value.powerMode == PowerMode.FORCE_CHARGE ||
                (settings.value.powerMode == PowerMode.AUTO && !liveBatteryInfo.value.isBypassActive)
            ) {
                val res = BatteryController.setChargeCurrentLimit(limitMa, settings.value.freezeThermalDaemons)
                _commandResult.value = res
            }
        }
    }

    fun setStopLevel(level: Int) {
        viewModelScope.launch {
            settingsRepo.updateStopLevel(level)
        }
    }

    fun setResumeLevel(level: Int) {
        viewModelScope.launch {
            settingsRepo.updateResumeLevel(level)
        }
    }

    fun setBypassMethod(method: BypassMethod) {
        viewModelScope.launch {
            settingsRepo.updateBypassMethod(method)
            if (liveBatteryInfo.value.isBypassActive) {
                BatteryController.enableBypass(method)
            }
        }
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateAutoStartOnBoot(enabled)
        }
    }

    fun setStickyNotification(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateStickyNotification(enabled)
        }
    }

    fun setSoundAlert(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateSoundAlert(enabled)
        }
    }

    fun setTempSafetyCutoff(temp: Int) {
        viewModelScope.launch {
            settingsRepo.updateTempCutoff(temp)
        }
    }

    fun setFreezeThermalDaemons(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateFreezeThermal(enabled)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            database.chargeLogDao().clearAllLogs()
        }
    }

    fun runCustomShellCommand(command: String) {
        viewModelScope.launch {
            val res = RootHelper.runRootCommand(command)
            _commandResult.value = res
            refreshDiagnostics()
        }
    }

    fun triggerTestSimulatedBypass() {
        viewModelScope.launch {
            val currentInfo = liveBatteryInfo.value
            database.chargeLogDao().insertLog(
                ChargeLogEntity(
                    tag = "TEST",
                    message = "Kiểm tra giả lập: Kích hoạt Ngắt dòng sạc Bypass (500mA test)",
                    batteryLevel = currentInfo.level,
                    temperature = currentInfo.temperature,
                    currentMa = 0,
                    voltage = currentInfo.voltage,
                    isBypassActive = true
                )
            )
        }
    }

    private fun ensureServiceStarted() {
        val app = getApplication<Application>()
        val intent = Intent(app, BatteryGuardService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                app.startForegroundService(intent)
            } else {
                app.startService(intent)
            }
        } catch (e: Exception) {
            // Ignore background start restrictions if any
        }
    }
}
