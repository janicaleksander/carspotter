package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserDream
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserDreamDao {

    @Upsert
    suspend fun insert(userDream: UserDream)

    @Upsert
    suspend fun insertAll(userDreams: List<UserDream>)

    @Query("SELECT * FROM user_dream WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<UserDream>

    @Query("SELECT * FROM user_dream WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun getSyncedForUser(userId: String): List<UserDream>

    @Query("""
        SELECT * FROM user_dream
        WHERE userId = :userId
    """)
    fun getAllUserDreams(userId: String): Flow<List<UserDream>>

    @Query("SELECT * FROM user_dream WHERE userId = :userId AND carId = :carId LIMIT 1")
    suspend fun findByUserAndCar(userId: String, carId: String): UserDream?

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM user_dream 
        WHERE userId = :userId AND carId = :carId AND syncState != 'PENDING_DELETE'
    )
""")
    fun observeIsDream(userId: String, carId: String): Flow<Boolean>
    @Query("""
        UPDATE user_dream
        SET syncState = 'PENDING_DELETE', updatedAt = :updatedAt
        WHERE userId = :userId AND carId = :carID
    """)
    suspend fun softDeleteUserDream(userId: String, carID: String, updatedAt: LocalDateTime)
    @Query("UPDATE user_dream SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM user_dream WHERE id = :id")
    suspend fun hardDelete(id: String)
}