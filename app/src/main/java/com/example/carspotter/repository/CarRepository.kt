package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.CarDao
import com.example.carspotter.dao.CarDetailsDao
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.dao.MediaDao
import com.example.carspotter.dao.UserCarDao
import com.example.carspotter.models.Car
import com.example.carspotter.models.CarDetails
import com.example.carspotter.models.CarWithDetails
import com.example.carspotter.models.CollectionTypeEnum
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.Location
import com.example.carspotter.models.Media
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.models.UserCar
import io.appwrite.Query
import io.appwrite.services.TablesDB
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Inject

class CarRepository @Inject constructor(
    private val tablesDB: TablesDB,
    private val userCarDao: UserCarDao,
    private val carDao: CarDao,
    private val carDetailsDao: CarDetailsDao,
    private val favouriteDao: FavouriteDao,
    private val mediaDao: MediaDao
) {
    private fun resolveId(value: Any?): String {
        return when (value) {
            is Map<*, *> -> value["\$id"] as String
            is String -> value
            else -> throw IllegalArgumentException("Cannot resolve id from $value")
        }
    }
    fun getTopCars(): Flow<List<CarWithDetails>> {
        return carDao.getAllTop()
    }

    fun getCarsFromCollection(userId: String, collectionId: String): Flow<List<Car>> {
        return userCarDao.getAllCarsByCollection(userId, collectionId)
    }

    fun getFavouriteCars(userId: String): Flow<List<Favourite>> {
        return favouriteDao.getAll(userId);
    }

    fun getCarCategory(carId: String): Flow<String?> {
        return carDao.getCategoryName(carId)
    }




    suspend fun syncCar(userId: String) {
        val limit = 100
        var offset: Int

        // 1. user_car — tylko dla danego użytkownika (potrzebujemy carId przed resztą)
        val allUserCars = mutableListOf<UserCar>()
        offset = 0
        do {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user_car",
                queries = listOf(
                    Query.limit(limit),
                    Query.offset(offset),
                    Query.equal("user.\$id", userId)
                )
            )
            val userCars = response.rows.map { row ->
                UserCar(
                    id = row.id,
                    userId = resolveId(row.data["user"]),
                    carId = resolveId(row.data["car"]),
                    collectionType = CollectionTypeEnum.fromValue(row.data["collectionType"] as String),
                    notes = row.data["notes"] as String,
                    location = Location(
                        latitude = (row.data["latitude"] as Number).toDouble(),
                        longitude = (row.data["longitude"] as Number).toDouble()
                    ),
                    addedAt = LocalDateTime.ofInstant(
                        OffsetDateTime.parse(row.data["\$createdAt"] as String).toInstant(),
                        ZoneId.systemDefault()
                    )
                )
            }
            allUserCars.addAll(userCars)
            offset += limit
        } while (userCars.size == limit)

        val userCarIds = allUserCars.map { it.carId }.distinct()

        // 2a. car — wszystkie isTop=true
        val allCars = mutableListOf<Car>()
        offset = 0
        do {
            val response = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "car",
                queries = listOf(
                    Query.limit(limit),
                    Query.offset(offset),
                    Query.equal("isTop", true)
                )
            )
            val cars = response.rows.map { row ->
                Car(
                    id = row.id,
                    brandId = resolveId(row.data["brand"]),
                    categoryId = resolveId(row.data["category"]),
                    model = row.data["model"] as String,
                    year = (row.data["year"] as Number).toInt(),
                    price = (row.data["price"] as Number).toDouble(),
                    isTop = row.data["isTop"] as Boolean
                )
            }
            allCars.addAll(cars)
            offset += limit
        } while (cars.size == limit)

        // 2b. car — auta usera z isTop=false (żeby nie duplikować isTop=true)
        if (userCarIds.isNotEmpty()) {
            userCarIds.chunked(100).forEach { chunk ->
                offset = 0
                do {
                    val response = tablesDB.listRows(
                        databaseId = BuildConfig.DATABASE_ID,
                        tableId = "car",
                        queries = listOf(
                            Query.limit(limit),
                            Query.offset(offset),
                            Query.equal("\$id", chunk),
                            Query.equal("isTop", false)
                        )
                    )
                    val cars = response.rows.map { row ->
                        Car(
                            id = row.id,
                            brandId = resolveId(row.data["brand"]),
                            categoryId = resolveId(row.data["category"]),
                            model = row.data["model"] as String,
                            year = (row.data["year"] as Number).toInt(),
                            price = (row.data["price"] as Number).toDouble(),
                            isTop = row.data["isTop"] as Boolean
                        )
                    }
                    allCars.addAll(cars)
                    offset += limit
                } while (cars.size == limit)
            }
        }

        carDao.insertAll(allCars)
        userCarDao.insertAll(allUserCars)


        // 3. car_detail — tylko dla isTop=true, filtrujemy po ID (bezpieczne)
        val topCarIds = allCars.filter { it.isTop }.map { it.id }.distinct()
        val allDetails = mutableListOf<CarDetails>()

        if (topCarIds.isNotEmpty()) {
            topCarIds.chunked(100).forEach { chunk ->
                offset = 0
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
                            carId = resolveId(row.data["car"]),
                            description = row.data["description"] as String,
                            powerHP = (row.data["powerHP"] as Number).toInt(),
                            acceleration = (row.data["acceleration"] as Number).toDouble(),
                            maxSpeed = (row.data["maxSpeed"] as Number).toDouble()
                        )
                    }
                    allDetails.addAll(details)
                    offset += limit
                } while (details.size == limit)
            }
        }

        carDetailsDao.insertAll(allDetails)

        // 4. media — dla wszystkich samochodów w Room (isTop + auta usera)
        val allCarIds = allCars.map { it.id }.distinct()
        val allMedia = mutableListOf<Media>()

        if (allCarIds.isNotEmpty()) {
            allCarIds.chunked(100).forEach { chunk ->
                offset = 0
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
                            carId = resolveId(row.data["car"]),
                            type = MediaTypeEnum.fromValue(row.data["type"] as String),
                            filePath = row.data["filePath"] as String,
                            createdAt = LocalDateTime.ofInstant(
                                OffsetDateTime.parse(row.data["\$createdAt"] as String).toInstant(),
                                ZoneId.systemDefault()
                            )
                        )
                    }
                    allMedia.addAll(medias)
                    offset += limit
                } while (medias.size == limit)
            }
        }

        mediaDao.insertAll(allMedia)

        val allFavourites = mutableListOf<Favourite>();
        offset = 0
        try{
            do {
                val favouriteResponse = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "favourite",
                    queries = listOf(
                        Query.equal("user", userId),
                        Query.limit(100),
                        Query.offset(offset)
                    )
                )

                val favourites = favouriteResponse.rows.map { row ->
                    Favourite(
                        row.id,
                        resolveId(row.data["user"]),
                        resolveId(row.data["car"])
                    )
                }
                allFavourites.addAll(favourites)
                offset += limit

            } while (favourites.size==limit)

            favouriteDao.insertAll(allFavourites)
        }catch (e: Exception){
            Log.d("SyncWorker","fav error")
            throw e;
        }
    }


}