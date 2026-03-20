package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.carspotter.models.Car
@Dao
interface CarDao {
    @Insert
    suspend fun insert(car: Car)

    @Query("SELECT * FROM car WHERE id = :id ")
    fun getById(id:Long): Car?

    @Delete
    fun delete(car: Car)

    @Query("DELETE FROM car WHERE id = :id")
    fun deleteById(id:Long)


}