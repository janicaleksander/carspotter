package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.car_spotter.models.Category
@Dao
interface CategoryDao {
    @Query("SELECT * FROM category")
    suspend fun getAll(): List<Category>
}