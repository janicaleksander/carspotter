package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Category
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

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)

    @Query("SELECT * FROM media")
    fun getAllMedia(): Flow<List<Media>>


    @Query("SELECT * FROM media WHERE type = 'photo'")
    fun getAllPhotos(): Flow<List<Media>>


    @Query("""
        SELECT m.* FROM media m
        INNER JOIN car c ON m.carId = c.id
        WHERE c.id = :carId
    """)
    fun getAllPhotosForCar(carId: String): Flow<List<Media>>
    @Query("""
        SELECT m.* FROM media m
        INNER JOIN car c ON m.carId = c.id
        WHERE c.isTop = 1
    """)
    fun getMediasForTopCar(): Flow<List<Media>>
}