package com.example.wassupguard.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.wassupguard.data.dao.ScanLogDao
import com.example.wassupguard.data.dao.SignatureDao
import com.example.wassupguard.data.entity.ScanLog
import com.example.wassupguard.data.entity.Signature

@Database(
    entities = [Signature::class, ScanLog::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun signatureDao(): SignatureDao
    abstract fun scanLogDao(): ScanLogDao
}


