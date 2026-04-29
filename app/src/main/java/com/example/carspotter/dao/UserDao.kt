package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.User

@Dao
interface UserDao {

    @Upsert
    suspend fun insert(user: User)

    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUser(userId: String): User?

    @Query("DELETE FROM user WHERE id = :userId")
    suspend fun deleteById(userId: String)
}
