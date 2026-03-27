package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.SettingDao
import com.example.carspotter.models.Settings
import io.appwrite.services.TablesDB
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingDao: SettingDao,
    private val tablesDB: TablesDB
) {
    suspend fun getSettings(): Settings {
        return settingDao.getNewest();
    }
    suspend fun syncSettings(){
        try {
            val settingsResponse = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "setting"
            )

            val setting = settingsResponse.rows.map { row ->
                Settings(
                    id = row.id,
                    appName = row.data["appName"] as String,
                    author = row.data["author"] as String,
                    version = row.data["version"] as String
                )
            }
            settingDao.insert(setting.first())
        } catch (e: Exception){
            throw e
        }
    }
}