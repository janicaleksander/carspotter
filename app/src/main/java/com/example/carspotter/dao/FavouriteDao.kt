package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
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
    suspend fun insertAll(favourites: List<Favourite>)

    @Query("SELECT * FROM favourite WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<Favourite>

    @Query("SELECT * FROM favourite WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun getSyncedForUser(userId: String): List<Favourite>

    @Query("SELECT * FROM favourite WHERE userId = :userId AND syncState != 'PENDING_DELETE'")
    fun getAll(userId: String): Flow<List<Favourite>>

    @Query("SELECT * FROM favourite WHERE userId = :userId AND carId = :carId LIMIT 1")
    suspend fun findByUserAndCar(userId: String, carId: String): Favourite?

    @Delete
    suspend fun delete(favourite: Favourite)

    @Query("DELETE FROM favourite WHERE id = :id")
    suspend fun hardDelete(id: String)

    @Query("UPDATE favourite SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("UPDATE favourite SET syncState = 'PENDING_DELETE', updatedAt = :updatedAt WHERE id = :id")
    suspend fun markAsPendingDelete(id: String, updatedAt: java.time.LocalDateTime)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM favourite " +
                "WHERE userId = :userId AND carId = :carId AND syncState != 'PENDING_DELETE')"
    )
    fun observeIsFavourite(userId: String, carId: String): Flow<Boolean>
}
