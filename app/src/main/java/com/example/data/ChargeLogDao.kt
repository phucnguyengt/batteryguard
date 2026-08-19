package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeLogDao {
    @Query("SELECT * FROM charge_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAllLogs(): Flow<List<ChargeLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChargeLogEntity)

    @Query("DELETE FROM charge_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM charge_logs")
    suspend fun getLogCount(): Int
}
