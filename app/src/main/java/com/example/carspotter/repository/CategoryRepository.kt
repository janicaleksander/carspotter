package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CategoryDao
import com.example.carspotter.models.Category
import com.example.carspotter.models.Converters
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val tablesDB: TablesDB
) {

    fun getCategories(): Flow<List<Category>> {
        return categoryDao.getAll();
    }

    suspend fun syncCategories() {
        val converters = Converters()
        val allCategories = mutableListOf<Category>()
        var offset = 0
        val limit = 100

        try {
            do {
                val response = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "category",
                    queries = listOf(
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )

                val categories = response.rows.map { row ->
                    Category(
                        id = row.id,
                        name = row.data["name"] as String,
                        updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                    )
                }

                allCategories.addAll(categories)
                offset += limit

            } while (categories.size == limit)

            categoryDao.insertAll(allCategories)

        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}