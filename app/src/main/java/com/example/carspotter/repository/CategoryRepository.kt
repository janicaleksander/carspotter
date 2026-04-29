package com.example.carspotter.repository

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


    fun getCategoryById(categoryId: String): Flow<Category?> {
        return categoryDao.getById(categoryId)
    }

    suspend fun syncCategories(): Set<String> {
        val converters = Converters()
        val allCategories = mutableListOf<Category>()
        var offset = 0
        val limit = 100

        return try {
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
            allCategories.map { it.id }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun pruneRemovedCategories(cloudIds: Set<String>) {
        categoryDao.getAllSnapshot()
            .filter { it.id !in cloudIds }
            .forEach { categoryDao.hardDeleteIfUnused(it.id) }
    }
}
