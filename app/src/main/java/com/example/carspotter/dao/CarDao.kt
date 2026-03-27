package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Car
import com.example.carspotter.models.Category

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories:List<Car>) //get only this with isTop


}