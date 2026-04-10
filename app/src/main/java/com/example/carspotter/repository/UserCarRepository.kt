package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.UserCarDao
import com.example.carspotter.models.Car
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Location
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserCar
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserCarRepository @Inject constructor(
    private val userCarDao: UserCarDao,
    private val tablesDB: TablesDB
) {


    fun getCarsFromGarage(userId: String): Flow<List<Car>> {
        return userCarDao.getAllUserCars(userId)
    }

    /**
     * Fetches user_car rows from Appwrite cloud, filtered by userId.
     * Does NOT insert into Room — call saveToRoom() after cars are synced.
     */
    suspend fun fetchFromCloud(userId: String): List<UserCar> {
        val converters = Converters()
        val allUserCars = mutableListOf<UserCar>()
        var offset = 0
        val limit = 100

        do {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user_car",
                queries = listOf(
                    Query.limit(limit),
                    Query.offset(offset),
                    Query.equal("user.\$id", userId)
                )
            )
            val userCars = response.rows.map { row ->
                UserCar(
                    id = row.id,
                    userId = converters.resolveId(row.data["user"]),
                    carId = converters.resolveId(row.data["car"]),
                    notes = row.data["notes"] as String,
                    location = Location(
                        latitude = (row.data["latitude"] as Number).toDouble(),
                        longitude = (row.data["longitude"] as Number).toDouble()
                    ),
                    updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now(),
                    syncState = SyncState.SYNCED
                )
            }
            allUserCars.addAll(userCars)
            offset += limit
        } while (userCars.size == limit)

        return allUserCars
    }

    /**
     * Inserts pre-fetched user_car rows into Room with conflict resolution.
     * Must be called AFTER cars are synced (FK constraint).
     *
     * Uses Last-Write-Wins strategy:
     * - If local record is SYNCED → always overwrite with cloud version
     * - If local record is PENDING_UPDATE/PENDING_DELETE and cloud is newer → cloud wins
     * - If local record is PENDING_UPDATE/PENDING_DELETE and local is newer → keep local
     */
    suspend fun saveToRoom(userCars: List<UserCar>) {
        val pendingRecords = userCarDao.getPendingRecords()
        val pendingMap = pendingRecords.associateBy { it.id }

        val toUpsert = mutableListOf<UserCar>()

        for (cloudRecord in userCars) {
            val localPending = pendingMap[cloudRecord.id]
            if (localPending == null) {
                // No local conflict — insert/update with cloud version
                toUpsert.add(cloudRecord)
            } else if (cloudRecord.updatedAt.isAfter(localPending.updatedAt)) {
                // Cloud is newer — cloud wins (Last-Write-Wins)
                toUpsert.add(cloudRecord)
            }
            // else: local is newer — skip cloud version, keep local pending change
        }

        if (toUpsert.isNotEmpty()) {
            userCarDao.insertAll(toUpsert)
        }
    }
}