package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.User
@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)
}