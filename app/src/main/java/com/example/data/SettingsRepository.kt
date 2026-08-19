package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppSettings
import com.example.model.BypassMethod
import com.example.model.PowerMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "battery_guard_prefs")

class SettingsRepository(private val context: Context) {

    private object PreferenceKeys {
        val IS_AUTO_MODE = booleanPreferencesKey("is_auto_mode")
        val POWER_MODE = stringPreferencesKey("power_mode")
        val STOP_LEVEL = intPreferencesKey("stop_level")
        val RESUME_LEVEL = intPreferencesKey("resume_level")
        val CHARGE_LIMIT_MA = intPreferencesKey("charge_limit_ma")
        val BYPASS_METHOD = stringPreferencesKey("bypass_method")
        val AUTO_START_ON_BOOT = booleanPreferencesKey("auto_start_on_boot")
        val SHOW_STICKY_NOTIFICATION = booleanPreferencesKey("show_sticky_notification")
        val SOUND_ALERT = booleanPreferencesKey("sound_alert")
        val TEMP_CUTOFF = intPreferencesKey("temp_cutoff")
        val FREEZE_THERMAL = booleanPreferencesKey("freeze_thermal")
        val IS_SERVICE_RUNNING = booleanPreferencesKey("is_service_running")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val powerModeName = preferences[PreferenceKeys.POWER_MODE] ?: PowerMode.AUTO.name
        val bypassMethodName = preferences[PreferenceKeys.BYPASS_METHOD] ?: BypassMethod.ZERO_CURRENT.name

        AppSettings(
            isAutoModeEnabled = preferences[PreferenceKeys.IS_AUTO_MODE] ?: true,
            powerMode = runCatching { PowerMode.valueOf(powerModeName) }.getOrDefault(PowerMode.AUTO),
            stopLevel = preferences[PreferenceKeys.STOP_LEVEL] ?: 80,
            resumeLevel = preferences[PreferenceKeys.RESUME_LEVEL] ?: 30,
            chargeLimitMa = preferences[PreferenceKeys.CHARGE_LIMIT_MA] ?: 0,
            bypassMethod = runCatching { BypassMethod.valueOf(bypassMethodName) }.getOrDefault(BypassMethod.ZERO_CURRENT),
            autoStartOnBoot = preferences[PreferenceKeys.AUTO_START_ON_BOOT] ?: true,
            showStickyNotification = preferences[PreferenceKeys.SHOW_STICKY_NOTIFICATION] ?: true,
            soundAlertOnThreshold = preferences[PreferenceKeys.SOUND_ALERT] ?: false,
            tempSafetyCutoff = preferences[PreferenceKeys.TEMP_CUTOFF] ?: 45,
            freezeThermalDaemons = preferences[PreferenceKeys.FREEZE_THERMAL] ?: false,
            isServiceRunning = preferences[PreferenceKeys.IS_SERVICE_RUNNING] ?: false
        )
    }

    suspend fun updatePowerMode(mode: PowerMode) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.POWER_MODE] = mode.name
            prefs[PreferenceKeys.IS_AUTO_MODE] = (mode == PowerMode.AUTO)
        }
    }

    suspend fun updateStopLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.STOP_LEVEL] = level.coerceIn(50, 95)
        }
    }

    suspend fun updateResumeLevel(level: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.RESUME_LEVEL] = level.coerceIn(15, 60)
        }
    }

    suspend fun updateChargeLimitMa(limitMa: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.CHARGE_LIMIT_MA] = limitMa
        }
    }

    suspend fun updateBypassMethod(method: BypassMethod) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.BYPASS_METHOD] = method.name
        }
    }

    suspend fun updateAutoStartOnBoot(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.AUTO_START_ON_BOOT] = enabled
        }
    }

    suspend fun updateStickyNotification(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SHOW_STICKY_NOTIFICATION] = enabled
        }
    }

    suspend fun updateSoundAlert(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SOUND_ALERT] = enabled
        }
    }

    suspend fun updateTempCutoff(temp: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.TEMP_CUTOFF] = temp
        }
    }

    suspend fun updateFreezeThermal(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.FREEZE_THERMAL] = enabled
        }
    }

    suspend fun updateServiceRunning(running: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.IS_SERVICE_RUNNING] = running
        }
    }
}
