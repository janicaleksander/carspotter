package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDao
import com.example.carspotter.models.Car
import com.example.carspotter.models.CarWithDetails
import com.example.carspotter.models.Converters
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor(
    private val tablesDB: TablesDB,
    private val carDao: CarDao
) {


    fun getTopCars(): Flow<List<CarWithDetails>> {
        return carDao.getAllTop()
    }

    fun getCarCategory(carId: String): Flow<String?> {
        return carDao.getCategoryName(carId)
    }

    /**
     * Syncs cars from Appwrite to Room:
     * - All isTop=true cars (pool for user_dream)
     * - User's isTop=false cars (identified by userCarIds)
     *
     * @param userCarIds list of car IDs from user_car (user's garage)
     * @return SyncedCarResult with allCarIds and topCarIds for downstream syncs
     */
    suspend fun syncCars(userCarIds: List<String>): SyncedCarResult {
        val converters = Converters()
        val limit = 100
        var offset: Int

        // 1. car — all isTop=true
        val allCars = mutableListOf<Car>()
        offset = 0
        do {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "car",
                queries = listOf(
                    Query.limit(limit),
                    Query.offset(offset),
                    Query.equal("isTop", true)
                )
            )
            val cars = response.rows.map { row ->
                Car(
                    id = row.id,
                    brandId = converters.resolveId(row.data["brand"]),
                    categoryId = converters.resolveId(row.data["category"]),
                    model = row.data["model"] as String,
                    year = (row.data["year"] as Number).toInt(),
                    price = (row.data["price"] as Number).toDouble(),
                    isTop = row.data["isTop"] as Boolean,
                    updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                )
            }
            allCars.addAll(cars)
            offset += limit
        } while (cars.size == limit)

        // 2. car — user's cars with isTop=false (avoid duplicating isTop=true)
        if (userCarIds.isNotEmpty()) {
            userCarIds.chunked(100).forEach { chunk ->
                offset = 0
                do {
                    val response = tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "car",
                        queries = listOf(
                            Query.limit(limit),
                            Query.offset(offset),
                            Query.equal("\$id", chunk),
                            Query.equal("isTop", false)
                        )
                    )
                    val cars = response.rows.map { row ->
                        Car(
                            id = row.id,
                            brandId = converters.resolveId(row.data["brand"]),
                            categoryId = converters.resolveId(row.data["category"]),
                            model = row.data["model"] as String,
                            year = (row.data["year"] as Number).toInt(),
                            price = (row.data["price"] as Number).toDouble(),
                            isTop = row.data["isTop"] as Boolean,
                            updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                        )
                    }
                    allCars.addAll(cars)
                    offset += limit
                } while (cars.size == limit)
            }
        }

        carDao.insertAll(allCars)

        return SyncedCarResult(
            allCarIds = allCars.map { it.id }.distinct(),
            topCarIds = allCars.filter { it.isTop }.map { it.id }.distinct()
        )
    }
}