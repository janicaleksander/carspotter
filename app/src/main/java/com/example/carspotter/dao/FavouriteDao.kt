package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.SyncState
import kotlinx.coroutines.flow.Flow
@Dao
interface FavouriteDao {
    @Upsert
    suspend fun insert(favourite: Favourite)
    @Upsert
    suspend fun insertAll(favourites:List<Favourite>)

    @Query("SELECT * FROM favourite WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<Favourite>

    @Query("SELECT * FROM favourite WHERE userId=:userId")
    fun getAll(userId: String): Flow<List<Favourite>>

    @Delete
    suspend fun delete(favourite: Favourite)

    @Query("DELETE FROM favourite WHERE id=:id")
    suspend fun deleteById(id: String)


    @Query("UPDATE favourite SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM favourite WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favourite WHERE userId = :userId AND carId = :carId AND syncState != 'DELETED')")
    fun observeIsDream(userId: String, carId: String): Flow<Boolean>


}
