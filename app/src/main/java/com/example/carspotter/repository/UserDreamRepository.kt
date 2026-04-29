package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.UserDreamDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserDream
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserDreamRepository @Inject constructor(
    private val userDreamDao: UserDreamDao,
    private val tablesDB: TablesDB
) {

    fun getCarsFromDreams(userId: String): Flow<List<UserDream>> {
        return userDreamDao.getAllUserDreams(userId)
    }

    fun observeIsDream(userId: String, carId: String): Flow<Boolean> {
        return userDreamDao.observeIsDream(userId, carId)
    }

    suspend fun addUserDream(userId:String, carId:String){
        userDreamDao.insert(
            UserDream(
                id = ID.unique(),
                userId = userId,
                carId = carId,
                updatedAt = LocalDateTime.now(),
                syncState = SyncState.PENDING_CREATE
            )
        )
    }

    suspend fun deleteUserDream(userId: String, carId: String){
        userDreamDao.softDeleUserDream(userId,carId)
    }

    /**
     * Fetches user_dream rows from Appwrite cloud, filtered by userId.
     * Does NOT insert into Room — call saveToRoom() after cars are synced.
     */
    suspend fun fetchFromCloud(userId: String): List<UserDream> {
        val converters = Converters()
        val allUserDreams = mutableListOf<UserDream>()
        var offset = 0
        val limit = 100

        do {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user_dream",
                queries = listOf(
                    Query.limit(limit),
                    Query.offset(offset),
                    Query.equal("user.\$id", userId)
                )
            )
            val userDreams = response.rows.map { row ->
                UserDream(
                    id = row.id,
                    userId = converters.resolveId(row.data["user"]),
                    carId = converters.resolveId(row.data["car"]),
                    updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now(),
                    syncState = SyncState.SYNCED
                )
            }
            allUserDreams.addAll(userDreams)
            offset += limit
        } while (userDreams.size == limit)

        return allUserDreams
    }

    /**
     * Merges cloud `user_dream` rows into Room.
     * Same Last-Write-Wins rules as [UserCarRepository.saveToRoom], plus pruning
     * of local SYNCED rows removed on the server.
     */
    suspend fun saveToRoom(userId: String, userDreams: List<UserDream>) {
        val cloudIds = userDreams.map { it.id }.toSet()
        userDreamDao.getSyncedForUser(userId)
            .filter { it.id !in cloudIds }
            .forEach { userDreamDao.hardDelete(it.id) }

        val pendingRecords = userDreamDao.getPendingRecords()
        val pendingMap = pendingRecords.associateBy { it.id }

        val toUpsert = mutableListOf<UserDream>()

        for (cloudRecord in userDreams) {
            val localPending = pendingMap[cloudRecord.id]
            if (localPending == null) {
                toUpsert.add(cloudRecord)
            } else if (cloudRecord.updatedAt.isAfter(localPending.updatedAt)) {
                toUpsert.add(cloudRecord)
            }
        }

        if (toUpsert.isNotEmpty()) {
            userDreamDao.insertAll(toUpsert)
        }
    }

    suspend fun pushPending(){
        val pending = userDreamDao.getPendingRecords()
        for(record in pending) {
            when (record.syncState) {
                SyncState.PENDING_CREATE -> {
                    try{
                        tablesDB.createRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "user_dream",
                            rowId = record.id,
                            data = mapOf(
                                "user" to record.userId,
                                "car" to record.carId
                            )
                        )
                        userDreamDao.markAsSynced(record.id)
                    }catch (e : Exception){
                        Log.e("UserDreamRepository", "Failed to push PENDING_CREATE for user dream ${record.id}", e)
                    }
                }
                SyncState.PENDING_DELETE -> {
                    try {
                        tablesDB.deleteRow(
                            databaseId = BuildConfig.DATABASE_ID,
                            tableId = "user_dream",
                            rowId = record.id
                        )
                        userDreamDao.hardDelete(record.id)
                    } catch (e: Exception) {
                        Log.e("UserDreamRepository", "Failed to push PENDING_DELETE for user dream ${record.id}", e)
                    }
                }

                else -> { /* nic */
                }
            }
        }

    }
}