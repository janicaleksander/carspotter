package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.car_spotter.models.Media
import kotlinx.coroutines.flow.Flow
@Dao
interface MediaDao {
    @Insert
    suspend fun inset(media: Media)

    @Query("SELECT * FROM media WHERE carId = :carId")
    fun getMediaByCarId(carId: Long): Flow<List<Media>>

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: Long)
}