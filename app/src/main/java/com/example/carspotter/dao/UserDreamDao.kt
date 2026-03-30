package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.UserDream
import kotlinx.coroutines.flow.Flow
@Dao
interface UserDreamDao {

    @Upsert
    suspend fun insertUserDream(userDream: UserDream)

    @Upsert
    suspend fun insertAll(userDreams: List<UserDream>)


    @Query("""
        SELECT * FROM user_dream
        WHERE userId = :userId
    """)
    fun getAllUserDreams(userId: String): Flow<List<UserDream>>
}