package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.UserDreamDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.SyncState
import com.example.carspotter.models.UserDream
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
     * Inserts pre-fetched user_dream rows into Room.
     * Must be called AFTER cars are synced (FK constraint).
     */
    suspend fun saveToRoom(userDreams: List<UserDream>) {
        userDreamDao.insertAll(userDreams)
    }
}