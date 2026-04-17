package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserDream
import kotlinx.coroutines.flow.Flow
@Dao
interface UserDreamDao {

    @Upsert
    suspend fun insertUserDream(userDream: UserDream)

    @Upsert
    suspend fun insertAll(userDreams: List<UserDream>)

    @Query("SELECT * FROM user_dream WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<UserDream>

    @Query("""
        SELECT * FROM user_dream
        WHERE userId = :userId
    """)
    fun getAllUserDreams(userId: String): Flow<List<UserDream>>

    @Query("""
    SELECT EXISTS(
        SELECT 1 FROM user_dream 
        WHERE userId = :userId AND carId = :carId
    )
""")
    fun observeIsDream(userId: String, carId: String): Flow<Boolean>
    @Query("""
        UPDATE user_dream
        SET syncState = 'PENDING_DELETE'
        WHERE userId = :userId AND carId = :carID
    """)
    suspend fun softDeleUserDream(userId: String,carID: String)
    @Query("UPDATE user_dream SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM user_dream WHERE id = :id")
    suspend fun hardDelete(id: String)
}