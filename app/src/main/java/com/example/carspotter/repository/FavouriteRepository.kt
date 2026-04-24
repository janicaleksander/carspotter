package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.SyncState
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavouriteRepository @Inject constructor(
    private val favouriteDao: FavouriteDao,
    private val tablesDB: TablesDB
) {


    fun getFavourites(userId: String): Flow<List<Favourite>> {
        return favouriteDao.getAll(userId)
    }

    fun observeIsFavourite(userId: String, carId: String): Flow<Boolean> {
        return favouriteDao.observeIsDream(userId, carId)
    }

    /**
     * Syncs favourite rows from Appwrite, filtered by userId.
     * Must be called AFTER cars are synced (FK constraint).
     */
    suspend fun syncFavourites(userId: String) {
        val converters = Converters()
        val allFavourites = mutableListOf<Favourite>()
        var offset = 0
        val limit = 100

        try {
            do {
                val response = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "favourite",
                    queries = listOf(
                        Query.equal("user.\$id", userId),
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )
                val favourites = response.rows.map { row ->
                    Favourite(
                        id = row.id,
                        userId = converters.resolveId(row.data["user"]),
                        carId = converters.resolveId(row.data["car"]),
                        updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now(),
                        syncState = SyncState.SYNCED
                    )
                }
                allFavourites.addAll(favourites)
                offset += limit
            } while (favourites.size == limit)

            // Conflict resolution (Last-Write-Wins)
            val pendingRecords = favouriteDao.getPendingRecords()
            val pendingMap = pendingRecords.associateBy { it.id }

            val toUpsert = mutableListOf<Favourite>()

            for (cloudRecord in allFavourites) {
                val localPending = pendingMap[cloudRecord.id]
                if (localPending == null) {
                    toUpsert.add(cloudRecord)
                } else if (cloudRecord.updatedAt.isAfter(localPending.updatedAt)) {
                    toUpsert.add(cloudRecord)
                }
            }

            if (toUpsert.isNotEmpty()) {
                favouriteDao.insertAll(toUpsert)
            }
        } catch (e: Exception) {
            Log.d("SyncWorker", "fav error")
            throw e
        }
    }

    suspend fun pushPending() {
        val pending = favouriteDao.getPendingRecords()
        for (record in pending) {
            when (record.syncState) {
                SyncState.PENDING_CREATE -> {
                    try{
                        tablesDB.createRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "favourite",
                            rowId = record.id,
                            data = mapOf(
                                "user" to record.userId,
                                "car" to record.carId
                            )
                        )
                        favouriteDao.markAsSynced(record.id)
                    }catch (e : Exception){
                        //todo
                    }
                }
                SyncState.PENDING_DELETE -> {
                    try{
                        tablesDB.deleteRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "favourite",
                            rowId = record.id
                        )
                        favouriteDao.hardDelete(record.id)
                    } catch (e : Exception){
                        //todo
                    }
                }
                else -> { /* nic */ }
            }
        }
    }
}