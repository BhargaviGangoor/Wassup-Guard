package com.example.wassupguard.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wassupguard.data.entity.Signature

@Dao
interface SignatureDao {
    @Query("SELECT * FROM signatures WHERE hash = :hash LIMIT 1")
    suspend fun getByHash(hash: String): Signature?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(signature: Signature)
}


