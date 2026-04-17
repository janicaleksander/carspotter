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
import com.example.carspotter.models.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(car: Car)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(cars: List<Car>)

    @Transaction
    @Query("SELECT * FROM car WHERE id = :id")
    suspend fun getById(id: String): Car?

    @Query("SELECT * FROM car WHERE id = :carId AND isTop = 1")
    fun getTopCarById(carId: String): Flow<CarWithDetails?>
    @Query("SELECT category.name FROM category INNER JOIN car ON category.id = car.categoryId WHERE car.id = :carId")
    fun getCategoryName(carId: String): Flow<String?>
    @Transaction
    @Query("SELECT * FROM car")
    fun getAll(): Flow<List<Car>>

    @Transaction
    @Query("SELECT * FROM car WHERE isTop = 1")
    fun getAllTop(): Flow<List<CarWithDetails>>

    @Transaction
    @Query("SELECT * FROM car WHERE isTop = 1 AND categoryId = :categoryId")
    fun getTopByCategory(categoryId: String): Flow<List<CarWithDetails>>

    @Delete
    suspend fun delete(car: Car)

    @Query("DELETE FROM car WHERE id = :id")
    suspend fun deleteById(id: String)


    @Query("SELECT * FROM car WHERE syncState != :state AND isTop = 0")
    suspend fun getPendingRecords(state: SyncState = SyncState.SYNCED): List<Car>

    @Query("UPDATE car SET syncState = 'SYNCED' WHERE id = :id")
    suspend fun markAsSynced(id: String)

    @Query("DELETE FROM car WHERE id = :id")
    suspend fun hardDelete(id: String)
}