package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.carspotter.models.Car
import com.example.carspotter.models.CollectionTypeEnum
import com.example.carspotter.models.UserCar
import kotlinx.coroutines.flow.Flow
@Dao
interface UserCarDao {

    @Query("SELECT carId FROM user_car WHERE userId = :userId AND collectionType = :collectionType")
    fun getAllCarsId(userId:Long,collectionType: String): Flow<List<Long>>

}