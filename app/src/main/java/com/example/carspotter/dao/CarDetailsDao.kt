package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.carspotter.models.CarDetails


@Dao
interface CarDetailsDao {

    @Upsert
    suspend fun insert(carDetails: CarDetails)

    @Upsert
    suspend fun insertAll(carDetailsList: List<CarDetails>)

    @Update
    suspend fun update(carDetails: CarDetails)

    @Query("SELECT * FROM car_detail")
    suspend fun getAll(): List<CarDetails>
    @Query("SELECT * FROM car_detail WHERE carId = :carId")
    suspend fun getByCarId(carId: String): CarDetails?

    @Delete
    suspend fun delete(carDetails: CarDetails)
}