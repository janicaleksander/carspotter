package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Category
import com.example.carspotter.models.Media
import kotlinx.coroutines.flow.Flow
@Dao
interface MediaDao {
    @Insert
    suspend fun inset(media: Media)

    @Query("SELECT * FROM media WHERE carId = :carId")
    fun getMediaByCarId(carId: String): Flow<List<Media>>

    @Query("DELETE FROM media WHERE id = :mediaId")
    suspend fun deleteMediaById(mediaId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories:List<Media>)//get only this with isTop
}