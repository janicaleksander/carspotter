package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.carspotter.models.Favourite
import kotlinx.coroutines.flow.Flow
@Dao
interface FavouriteDao {
    @Insert
    suspend fun insert(favouriteDao: Favourite)

    @Delete
    suspend fun delete(favouriteDao: Favourite)

    @Query("DELETE FROM favourite WHERE id=:id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM favourite WHERE userId=:userId")
    fun getAll(userId: String): Flow<List<Favourite>>


    @Insert
    fun insertAll(favourites:List<Favourite>)
}
//It's worth to add Flow to every get statements
//because then we don't have to fetch every time we change the screen, we can just observe the data,
// and it will update automatically when it changes.