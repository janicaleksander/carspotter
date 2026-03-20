package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.carspotter.models.Settings

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting ORDER BY id DESC LIMIT 1")
    suspend fun getNewest(): Settings
}