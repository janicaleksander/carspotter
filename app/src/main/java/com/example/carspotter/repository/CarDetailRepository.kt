package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDetailsDao
import com.example.carspotter.models.CarDetails
import com.example.carspotter.models.Converters
import io.appwrite.Query
import io.appwrite.services.TablesDB
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarDetailRepository @Inject constructor(
    private val carDetailsDao: CarDetailsDao,
    private val tablesDB: TablesDB
) {


    suspend fun getByCarId(carId: String): CarDetails? {
        return carDetailsDao.getByCarId(carId)
    }

    /**
     * Syncs car_detail from Appwrite for isTop=true cars only.
     * Each top car has exactly one car_detail (1:1 relationship).
     */
    suspend fun syncCarDetails(topCarIds: List<String>) {
        val converters = Converters()
        val allDetails = mutableListOf<CarDetails>()
        val limit = 100

        if (topCarIds.isNotEmpty()) {
            topCarIds.chunked(100).forEach { chunk ->
                var offset = 0
                do {
                    val response = tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "car_detail",
                        queries = listOf(
                            Query.limit(limit),
                            Query.offset(offset),
                            Query.equal("car.\$id", chunk)
                        )
                    )
                    val details = response.rows.map { row ->
                        CarDetails(
                            carId = converters.resolveId(row.data["car"]),
                            description = row.data["description"] as String,
                            powerHP = (row.data["powerHP"] as Number).toInt(),
                            acceleration = (row.data["acceleration"] as Number).toDouble(),
                            maxSpeed = (row.data["maxSpeed"] as Number).toDouble(),
                            updatedAt = converters.toLocalDateTime(row.updatedAt) ?: LocalDateTime.now()
                        )
                    }
                    allDetails.addAll(details)
                    offset += limit
                } while (details.size == limit)
            }
        }

        carDetailsDao.insertAll(allDetails)
    }
}