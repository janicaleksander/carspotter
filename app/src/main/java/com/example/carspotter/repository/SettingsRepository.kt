package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.SettingDao
import com.example.carspotter.models.Converters
import com.example.carspotter.models.Settings
import io.appwrite.Query
import io.appwrite.services.Storage
import io.appwrite.services.TablesDB
import java.time.LocalDateTime
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingDao: SettingDao,
    private val tablesDB: TablesDB,
    private val storage: Storage
) {
    suspend fun getSettings(): Settings? {
        return settingDao.getNewest()
    }

    suspend fun syncSettings() {
        val converters = Converters()
        try {
            val settingsResponse = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "setting"
            )

            val settings = settingsResponse.rows.map { row ->
                Settings(
                    id = row.id,
                    appName = row.data["appName"] as String,
                    author = row.data["author"] as String,
                    version = row.data["version"] as String,
                    updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                )
            }

            val newest = settings.maxByOrNull { it.updatedAt }
            if (newest != null) {
                settingDao.insert(newest)
                settingDao.deleteAllExcept(newest.id)
            } else {
                settingDao.deleteAll()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getRandomPhoto(bucketId: String): String {
        val files = storage.listFiles(
            bucketId = bucketId,
            queries = listOf(Query.limit(5))
        ).files

        val imageFiles = files.filter { file ->
            file.mimeType.startsWith("image/")
        }

        if (imageFiles.isEmpty()) return ""

        val randomFile = imageFiles.random()
        return "${BuildConfig.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/$bucketId/files/${randomFile.id}/preview" +
                "?project=${BuildConfig.APPWRITE_PROJECT_ID}" +
                "&width=1920" +
                "&quality=85" +
                "&output=webp"
    }
}
