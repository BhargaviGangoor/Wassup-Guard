package com.example.wassupguard.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signatures")
data class Signature(
    @PrimaryKey val hash: String,
    val threatLabel: String?,
    val source: String, // e.g., "local", "virustotal"
    val lastUpdatedEpochMillis: Long
)


