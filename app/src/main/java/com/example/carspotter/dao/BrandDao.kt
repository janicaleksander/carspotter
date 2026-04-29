package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Brand
import kotlinx.coroutines.flow.Flow

@Dao
interface BrandDao {

    @Upsert
    suspend fun insert(brand: Brand)

    @Upsert
    suspend fun insertAll(brands: List<Brand>)

    @Query("SELECT * FROM brand")
    fun getAll(): Flow<List<Brand>>

    @Query("SELECT * FROM brand WHERE id = :brandId")
    fun getById(brandId: String): Flow<Brand?>

    @Query("SELECT * FROM brand")
    suspend fun getAllSnapshot(): List<Brand>

    @Query(
        """
        DELETE FROM brand
        WHERE id = :id
          AND NOT EXISTS (SELECT 1 FROM car WHERE car.brandId = :id)
        """
    )
    suspend fun hardDeleteIfUnused(id: String)

    @Delete
    suspend fun delete(brand: Brand)
}
