package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.carspotter.models.Brand
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {
    @Query("SELECT * FROM brand")
    fun getAll(): Flow<List<Brand>>

    @Insert
    suspend fun insertAll(brands: List<Brand>)
}