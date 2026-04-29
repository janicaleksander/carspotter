package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Media
import kotlinx.coroutines.flow.Flow
@Dao
interface MediaDao {
    @Upsert
    suspend fun insert(media: Media)

    @Upsert
    suspend fun insertAll(categories:List<Media>)

    @Query("SELECT * FROM media WHERE carId = :carId")
    fun getMediaByCarId(carId: String): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE carId IN (:carIds) AND type = :type")
    fun getMediaByCarIdsAndType(carIds: List<String>, type: String): Flow<List<Media>>

    @Query("SELECT * FROM media WHERE carId = :carId")
    suspend fun getMediaByCarIdSnapshot(carId: String): List<Media>

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)

    @Query("SELECT * FROM media")
    suspend fun getAllMediaSnapshot(): List<Media>
}
