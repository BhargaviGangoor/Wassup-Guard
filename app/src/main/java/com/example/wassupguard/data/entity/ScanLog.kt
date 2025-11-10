package com.example.wassupguard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_logs")
data class ScanLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val hashSha256: String,
    val verdict: String, // Safe, Suspicious, Malicious
    val timestampEpochMillis: Long
)


