package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.MediaDao
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import io.appwrite.Query
import io.appwrite.services.TablesDB
import java.time.LocalDateTime
import javax.inject.Inject

class MediaRepository @Inject constructor(
    private val mediaDao: MediaDao,
    private val tablesDB: TablesDB
) {

    //get itd

    suspend fun syncMedia(userId: String) {
        val allMedias = mutableListOf<Media>()
        val limit = 25
        var offset = 0
        var medias: List<Media>

        try {
            do {
                val mediaResponse = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "media",
                    queries = listOf(
                        Query.or(listOf(
                            Query.equal("userId", userId),
                            Query.isNull("userId")
                        )),
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )

                medias = mediaResponse.rows.map { row ->
                    Media(
                        id = row.id,
                        carId = row.data["carId"] as? String ?: "",
                        userId = row.data["userId"] as? String,
                        type = MediaTypeEnum.fromValue(row.data["type"] as? String ?: ""),
                        filePath = row.data["filePath"] as? String ?: "",
                        createdAt = row.data["createdAt"]?.let {
                            LocalDateTime.parse(it as? String)
                        } ?: LocalDateTime.now()
                    )
                }

                allMedias.addAll(medias)
                offset += limit

            } while (medias.size == limit)

            mediaDao.insertAll(allMedias)

        } catch (e: Exception) {
            throw e
        }
    }
}