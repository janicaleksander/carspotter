package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDao
import com.example.carspotter.dao.UserCarDao
import com.example.carspotter.models.Car
import com.example.carspotter.models.CollectionTypeEnum
import com.example.carspotter.models.Location
import com.example.carspotter.models.UserCar
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

//TODO check null when we are creating objects
// i mean check models and then handle it when needed
@Singleton
class UserCarRepository  @Inject constructor(
    private val userCarDao: UserCarDao,
    private val carDao: CarDao,
    private val tablesDB: TablesDB
){
    fun getUserCars(userId:String): Flow<List<Car>> {
        return userCarDao.getAllCarForUser(userId)
    }
    suspend fun syncCar(userId: String) {
        val limit = 100
        val allUserCars = mutableListOf<UserCar>()

        try {
            var offset = 0
            do {
                val userCarResponse = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "user_car",
                    queries = listOf(
                        Query.equal("userId", userId),
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )
                val userCars = userCarResponse.rows.map { row ->
                    UserCar(
                        id = row.id,
                        userId = userId,
                        carId = row.data["carId"] as String,
                        collectionType = CollectionTypeEnum.fromValue(row.data["collectionType"] as String),
                        notes = row.data["notes"] as? String ?: "",
                        location = Location(
                            latitude = (row.data["location.latitude"] as Number).toDouble(),
                            longitude = (row.data["location.longitude"] as Number).toDouble()
                        ),
                        addedAt = LocalDateTime.parse(row.data["createdAt"] as String)
                    )
                }
                allUserCars.addAll(userCars)
                offset += limit
            } while (userCarResponse.rows.size == limit)

            val spottedCarIds = allUserCars.map { it.carId }.distinct()
            val allCars = mutableListOf<Car>()
            var carOffset = 0

            do {
                val carResponse = if (spottedCarIds.isEmpty()) {
                    tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "car",
                        queries = listOf(
                            Query.equal("isTop", true),
                            Query.limit(limit),
                            Query.offset(carOffset)
                        )
                    )
                } else {
                    tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "car",
                        queries = listOf(
                            Query.or(listOf(
                                Query.equal("isTop", true),
                                Query.contains("\$id", spottedCarIds)
                            )),
                            Query.limit(limit),
                            Query.offset(carOffset)
                        )
                    )
                }

                val cars = carResponse.rows.map { row ->
                    Car(
                        id = row.id,
                        brand = row.data["brand"] as String,
                        model = row.data["model"] as String,
                        year = (row.data["year"] as Number).toInt(),
                        price = (row.data["price"] as Number).toDouble(),
                        description = row.data["description"] as String,
                        category = row.data["category"] as String,
                        isTop = row.data["isTop"] as Boolean,
                        powerHP = row.data["powerHP"]?.let { (it as Number).toInt() },
                        acceleration = row.data["acceleration"]?.let { (it as Number).toDouble() },
                        maxSpeed = row.data["maxSpeed"]?.let { (it as Number).toDouble() },
                    )
                }
                allCars.addAll(cars)
                carOffset += limit
            } while (carResponse.rows.size == limit)

            carDao.insertAll(allCars)
            userCarDao.insertAll(allUserCars)

        } catch (e: Exception) {
            Log.e("UserCarRepository", "Error syncing user cars: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }


}