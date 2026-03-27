package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.BrandDao
import com.example.carspotter.models.Brand
import io.appwrite.Query
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
    // BrandRepository
    suspend fun syncBrands() {
        val allBrands = mutableListOf<Brand>()
        var offset = 0
        val limit = 25
        try {
            do {
                val response = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "brand",
                    queries = listOf(
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )
                val brands = response.rows.map { row ->
                    Brand(id = row.id, name = row.data["name"]?.toString() ?: "Unknown")
                }
                allBrands.addAll(brands)
                offset += limit
            } while (brands.size == limit)

            brandDao.insertAll(allBrands)
        }catch (e: Exception){
            throw e
        }
    }
}