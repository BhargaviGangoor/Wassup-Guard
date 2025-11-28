package com.example.wassupguard.data.dao

import androidx.room.Dao
import androidx.room.Delete
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

    @Delete
    suspend fun delete(log: ScanLog)

    @Query("DELETE FROM scan_logs")
    suspend fun deleteAll()
}
