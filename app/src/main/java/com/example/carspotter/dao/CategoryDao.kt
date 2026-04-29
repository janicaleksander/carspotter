package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Upsert
    suspend fun insert(category: Category)


    @Upsert
    suspend fun insertAll(categories:List<Category>)

    @Query("SELECT * FROM category")
    fun getAll(): Flow<List<Category>>

    @Query("SELECT * FROM category WHERE id = :categoryId")
    fun getById(categoryId: String): Flow<Category?>

    @Query("SELECT * FROM category")
    suspend fun getAllSnapshot(): List<Category>

    @Query(
        """
        DELETE FROM category
        WHERE id = :id
          AND NOT EXISTS (SELECT 1 FROM car WHERE car.categoryId = :id)
        """
    )
    suspend fun hardDeleteIfUnused(id: String)

    @Delete
    suspend fun delete(category: Category)
}
