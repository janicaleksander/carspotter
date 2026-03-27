package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.carspotter.models.Car
import com.example.carspotter.models.UserCar
import kotlinx.coroutines.flow.Flow
@Dao
interface UserCarDao {

    @Query("""
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userId AND user_car.collectionType = :collectionType
    """)
    fun getAllCarsForUserByCollection(userId: String, collectionType: String): Flow<List<Car>>

    @Query("""
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userID
    """)
    fun getAllCarForUser(userID: String): Flow<List<Car>>


    @Insert
    suspend fun insertCarForUser(userCar: UserCar)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(userCars: List<UserCar>)
}