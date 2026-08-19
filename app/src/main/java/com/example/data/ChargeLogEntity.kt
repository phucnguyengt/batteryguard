package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charge_logs")
data class ChargeLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String, // e.g. "BYPASS", "CHARGING", "THRESHOLD", "CURRENT_LIMIT", "WARNING", "BOOT"
    val message: String,
    val batteryLevel: Int,
    val temperature: Float,
    val currentMa: Int,
    val voltage: Float,
    val isBypassActive: Boolean
)
