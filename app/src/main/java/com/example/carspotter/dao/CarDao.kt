package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {
    @Insert
    suspend fun insert(car: Car)

    @Query("SELECT * FROM car WHERE id = :id ")
    fun getById(id: String): Car?

    @Delete
    fun delete(car: Car)

    @Query("DELETE FROM car WHERE id = :id")
    fun deleteById(id: String)

    @Query("SELECT * FROM car")
    fun getAll(): Flow<List<Car>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cars:List<Car>) //get only this with isTop and only with userID id


}