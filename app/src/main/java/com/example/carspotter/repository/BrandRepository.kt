package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.BrandDao
import com.example.carspotter.models.Brand
import io.appwrite.services.Databases
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrandRepository @Inject constructor(
    private val brandDao: BrandDao,
    private val tablesDB: TablesDB
) {

    fun getBrands(): Flow<List<Brand>> {
        return brandDao.getAll()
    }
    suspend fun syncBrands() {
        try {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "brand"
            )

            val brands = response.rows.map { row ->
                Brand(
                    id = row.id,
                    name = row.data["name"]?.toString() ?: "Unknown"
                )
            }

            brandDao.insertAll(brands)

        } catch (e: Exception) {
            //TODO errors
            e.printStackTrace()
        }
    }
}