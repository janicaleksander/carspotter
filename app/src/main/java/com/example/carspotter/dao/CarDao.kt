package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.carspotter.models.Car
import com.example.carspotter.models.CarWithDetails
import com.example.carspotter.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(car: Car)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cars: List<Car>)

    @Transaction
    @Query("SELECT * FROM car WHERE id = :id")
    suspend fun getById(id: String): CarWithDetails?

    @Transaction
    @Query("SELECT * FROM car")
    fun getAll(): Flow<List<CarWithDetails>>

    @Transaction
    @Query("SELECT * FROM car WHERE isTop = 1")
    fun getAllTop(): Flow<List<CarWithDetails>>

    @Delete
    suspend fun delete(car: Car)

    @Query("DELETE FROM car WHERE id = :id")
    suspend fun deleteById(id: String)
}