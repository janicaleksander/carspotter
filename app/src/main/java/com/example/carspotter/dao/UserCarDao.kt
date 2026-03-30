package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Car
import com.example.carspotter.models.UserCar
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCarDao {


    @Upsert
    suspend fun insert(userCar: UserCar)

    @Upsert
    suspend fun insertAll(userCars: List<UserCar>)

    @Query("""
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userId AND user_car.collectionType = :collectionType
    """)
    fun getAllCarsByCollection(userId: String, collectionType: String): Flow<List<Car>>

    @Query("""
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userId
    """)
    fun getAllUserCars(userId: String): Flow<List<Car>>


}