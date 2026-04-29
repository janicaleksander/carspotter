package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.MediaDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao,
    private val tablesDB: TablesDB,
    private val storage: Storage
) {

    fun getMediaForCar(carId: String): Flow<List<Media>> {
        return mediaDao.getMediaByCarId(carId)
    }

    fun getPhotoMediaForCars(carIds: List<String>): Flow<List<Media>> {
        if (carIds.isEmpty()) return flowOf(emptyList())
        return mediaDao.getMediaByCarIdsAndType(carIds.distinct(), MediaTypeEnum.PHOTO.value)
    }



    /**
     * Syncs media from Appwrite for all synced cars (both isTop=true and user's isTop=false).
     * Must be called AFTER cars are synced (FK constraint).
     */

    //suspend fun uploadMedia() -> url
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

        val cloudIds = allMedia.map { it.id }.toSet()
        mediaDao.getAllMediaSnapshot()
            .filter { it.carId in carIds && it.id !in cloudIds }
            .forEach { mediaDao.deleteMediaById(it.id) }

        mediaDao.insertAll(allMedia)
    }

    /**
     * Uploads media files to Appwrite Storage, creates the corresponding
     * `media` rows in the cloud and mirrors them in Room.
     *
     * Online-only — callers must gate this on network availability. We upload
     * the file first so we can persist the public URL (not the local file
     * path) as `filePath`, which is what other clients consume.
     */
    suspend fun uploadAndSaveMedia(carId: String, mediaList: List<Media>) {
        if (mediaList.isEmpty()) return

        val savedMedia = mediaList.map { local ->
            val fileId = ID.unique()
            storage.createFile(
                bucketId = BuildConfig.BUCKET_ID,
                fileId = fileId,
                file = InputFile.fromPath(local.filePath)
            )
            val cloudUrl = buildFileUrl(fileId)

            val rowId = ID.unique()
            tablesDB.createRow(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "media",
                rowId = rowId,
                data = mapOf(
                    "car" to carId,
                    "type" to local.type.value,
                    "filePath" to cloudUrl,
                )
            )

            local.copy(
                id = rowId,
                carId = carId,
                filePath = cloudUrl,
                updatedAt = LocalDateTime.now(),
            )
        }

        mediaDao.insertAll(savedMedia)
    }

    /**
     * Hard-deletes every media that belongs to [carId] from Appwrite — both the
     * file in Storage and the row in the `media` table. Best-effort: any single
     * failure is swallowed so one orphan does not block the rest of the cleanup.
     *
     * Must be called BEFORE the parent car row is hard-deleted from Room,
     * otherwise the FK cascade wipes the local rows we need to read fileIds from.
     */
    suspend fun deleteAllForCar(carId: String) {
        val medias = mediaDao.getMediaByCarIdSnapshot(carId)
        if (medias.isEmpty()) return

        medias.forEach { media ->
            extractFileId(media.filePath)?.let { fileId ->
                runCatching {
                    storage.deleteFile(
                        bucketId = BuildConfig.BUCKET_ID,
                        fileId = fileId,
                    )
                }
            }
            runCatching {
                tablesDB.deleteRow(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "media",
                    rowId = media.id,
                )
            }
        }
    }

    private fun buildFileUrl(fileId: String): String =
        "${BuildConfig.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${BuildConfig.BUCKET_ID}/files/$fileId/view?project=${BuildConfig.APPWRITE_PROJECT_ID}"

    private fun extractFileId(url: String): String? =
        FILE_ID_REGEX.find(url)?.groupValues?.getOrNull(1)

    companion object {
        private val FILE_ID_REGEX = Regex("""/files/([^/]+)/(?:view|preview|download)""")
    }
}
