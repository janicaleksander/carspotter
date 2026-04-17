package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.MediaDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao,
    private val tablesDB: TablesDB
) {

    fun getMediaForCar(carId: String): Flow<List<Media>> {
        return mediaDao.getMediaByCarId(carId)
    }

    fun getAllMedia(): Flow<List<Media>> {
        return mediaDao.getAllMedia()
    }
    fun getMediasForTopCar(): Flow<List<Media>> {
        return mediaDao.getMediasForTopCar()
    }

    fun getAllPhotos(): Flow<List<Media>> {
        return mediaDao.getAllPhotos()
    }

    /**
     * Syncs media from Appwrite for all synced cars (both isTop=true and user's isTop=false).
     * Must be called AFTER cars are synced (FK constraint).
     */
    suspend fun syncMedia(carIds: List<String>) {
        val converters = Converters()
        val allMedia = mutableListOf<Media>()
        val limit = 100

        if (carIds.isNotEmpty()) {
            carIds.chunked(100).forEach { chunk ->
                var offset = 0
                do {
                    val response = tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "media",
                        queries = listOf(
                            Query.limit(limit),
                            Query.offset(offset),
                            Query.equal("car.\$id", chunk)
                        )
                    )
                    val medias = response.rows.map { row ->
                        Media(
                            id = row.id,
                            carId = converters.resolveId(row.data["car"]),
                            type = MediaTypeEnum.fromValue(row.data["type"] as String),
                            filePath = row.data["filePath"] as String,
                            updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                        )
                    }
                    allMedia.addAll(medias)
                    offset += limit
                } while (medias.size == limit)
            }
        }

        mediaDao.insertAll(allMedia)
    }
}