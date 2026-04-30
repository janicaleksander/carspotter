package com.example.carspotter.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCar
import com.example.carspotter.models.UserCarInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface UserCarDao {

    @Upsert
    suspend fun insert(userCar: UserCar)

    @Upsert
    suspend fun insertAll(userCars: List<UserCar>)

    @Query("SELECT * FROM user_car WHERE syncState != :state")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<UserCar>

    @Query("SELECT * FROM user_car WHERE userId = :userId AND syncState = 'SYNCED'")
    suspend fun getSyncedForUser(userId: String): List<UserCar>

    @Query(
        """
        SELECT car.* FROM car
        INNER JOIN user_car ON car.id = user_car.carId
        WHERE user_car.userId = :userId
          AND user_car.syncState != 'PENDING_DELETE'
        """
    )
    fun getAllUserCars(userId: String): Flow<List<UserCarInfo>>

    @Query(
        """
        SELECT * FROM user_car
        WHERE userId = :userId AND carId = :carId
        LIMIT 1
        """
    )
    fun observeUserCar(userId: String, carId: String): Flow<UserCar?>

    @Query(
        """
        UPDATE user_car
        SET syncState = 'PENDING_DELETE', updatedAt = :updatedAt
        WHERE userId = :userId AND carId = :carId
        """
    )
    suspend fun softDeleteUserCar(userId: String, carId: String, updatedAt: LocalDateTime)

    @Query("UPDATE user_car SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM user_car WHERE id = :id")
    suspend fun hardDelete(id: String)
}
