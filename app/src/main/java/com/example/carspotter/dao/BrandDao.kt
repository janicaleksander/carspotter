package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.carspotter.models.Brand

@Dao
interface BrandDao {
    @Query("SELECT * FROM brand")
    suspend fun getAll(): List<Brand>
}