package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.Car
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCar
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCarDao {


    @Upsert
    suspend fun insert(userCar: UserCar)

    @Upsert
    suspend fun insertAll(userCars: List<UserCar>)

    @Query("SELECT * FROM user_car WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<UserCar>

    @Query("""
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userId
    """)
    fun getAllUserCars(userId: String): Flow<List<Car>>

    @Query("UPDATE user_car SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM user_car WHERE id = :id")
    suspend fun hardDelete(id: String)
}