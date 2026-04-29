package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDao
import com.example.carspotter.models.Car
import com.example.carspotter.models.CarWithDetails
import com.example.carspotter.models.Converters
import com.example.carspotter.models.SyncState
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarRepository @Inject constructor(
    private val tablesDB: TablesDB,
    private val carDao: CarDao,
    private val mediaRepository: MediaRepository,
) {


    fun getTopCars(): Flow<List<CarWithDetails>> {
        return carDao.getAllTop()
    }

    fun getTopCarById(carId: String): Flow<CarWithDetails?> {
        return carDao.getTopCarById(carId)
    }

    fun getCarById(carId: String): Flow<Car?> {
        return carDao.getCarWithDetailsById(carId)
    }

    /**
     * Soft-deletes a user-owned car (isTop=false only).
     * No-op for shared top cars. Picked up by [pushPending] on next sync.
     */
    suspend fun softDeleteUserCar(carId: String) {
        carDao.softDeleteUserCar(carId, LocalDateTime.now())
    }
    fun getTopCarsByCategory(categoryId: String): Flow<List<CarWithDetails>> {
        return carDao.getTopByCategory(categoryId)
    }

    fun getCarCategory(carId: String): Flow<String?> {
        return carDao.getCategoryName(carId)
    }

    suspend fun insertCar(car: Car) {
        carDao.insert(Car(
            id = car.id,
            brandId = car.brandId,
            categoryId = car.categoryId,
            model = car.model,
            year = car.year,
            price = car.price,
            isTop = car.isTop,
            updatedAt = LocalDateTime.now(),
            syncState = SyncState.PENDING_CREATE
        ))
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

        val topCars = allCars.filter { it.isTop }
        val userCars = allCars.filter { !it.isTop }
        val cloudIds = allCars.map { it.id }.toSet()

        carDao.getSyncedRecords()
            .filter { it.id !in cloudIds }
            .forEach { carDao.hardDelete(it.id) }

        carDao.insertAll(topCars)

        val pending = carDao.getPendingRecords().associateBy { it.id }
        val toUpsert = userCars.filter { cloud ->
            val local = pending[cloud.id]
            local == null || cloud.updatedAt.isAfter(local.updatedAt)
        }
        carDao.insertAll(toUpsert)

        return SyncedCarResult(
            allCarIds = allCars.map { it.id }.distinct(),
            topCarIds = allCars.filter { it.isTop }.map { it.id }.distinct()
        )
    }

    suspend fun pushPending() {
        val pending = carDao.getPendingRecords()

        for (car in pending) {
            try {
                when (car.syncState) {
                    SyncState.PENDING_CREATE -> {
                        tablesDB.createRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "car",
                            rowId = car.id,
                            data = mapOf(
                                "brand" to car.brandId,
                                "category" to car.categoryId,
                                "model" to car.model,
                                "year" to car.year,
                                "price" to car.price,
                                "isTop" to car.isTop
                            )
                        )
                        carDao.markAsSynced(car.id)
                    }
                    SyncState.PENDING_UPDATE -> {
                        tablesDB.updateRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "car",
                            rowId = car.id,
                            data = mapOf(
                                "model" to car.model,
                                "year" to car.year,
                                "price" to car.price
                            )
                        )
                        carDao.markAsSynced(car.id)
                    }
                    SyncState.PENDING_DELETE -> {
                        // Clean up media (Storage files + media rows) BEFORE the
                        // local cascade wipes the rows we need to read fileIds from,
                        // and BEFORE deleting the parent car row in Appwrite so we
                        // don't leave dangling references on the cloud side.
                        mediaRepository.deleteAllForCar(car.id)
                        tablesDB.deleteRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "car",
                            rowId = car.id
                        )
                        carDao.hardDelete(car.id)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Skip this record; SyncWorker will retry on the next pass.
            }
        }
    }
}
/*TODO ciekawe trzeba uwazac na usuwanie kaskadowe offline*/
