package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.car_spotter.models.User
@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Insert
    suspend fun insertUser(user: User): Long
}