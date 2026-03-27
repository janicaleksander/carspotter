package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Settings

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting ORDER BY id DESC LIMIT 1")
    suspend fun getNewest(): Settings

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(settings: Settings)
}