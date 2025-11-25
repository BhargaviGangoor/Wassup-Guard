package com.example.wassupguard.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wassupguard_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}


