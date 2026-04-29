package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Settings

@Dao
interface SettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: Settings)

    @Query("SELECT * FROM setting ORDER BY id DESC LIMIT 1")
    suspend fun getNewest(): Settings?

    @Query("DELETE FROM setting")
    suspend fun deleteAll()

    @Query("DELETE FROM setting WHERE id != :id")
    suspend fun deleteAllExcept(id: String)
}
