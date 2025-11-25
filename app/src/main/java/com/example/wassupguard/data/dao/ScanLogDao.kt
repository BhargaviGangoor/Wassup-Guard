package com.example.wassupguard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.wassupguard.data.entity.ScanLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanLogDao {
    @Insert
    suspend fun insert(log: ScanLog)

    @Query("SELECT * FROM scan_logs ORDER BY timestampEpochMillis DESC")
    fun observeAll(): Flow<List<ScanLog>>
}


