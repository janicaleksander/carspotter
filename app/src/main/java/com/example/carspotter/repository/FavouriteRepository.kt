package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.models.Favourite
import io.appwrite.Query
import io.appwrite.services.TablesDB
import javax.inject.Inject

class FavouriteRepository @Inject constructor(
    private val favouriteDao: FavouriteDao,
    private val tablesDB: TablesDB
) {
   //todo i dont know what to do with userID, do i have to pass it down?
    //fun getFavourites()

    suspend fun syncFavourites(userId:String){
        try {
            val allFavourites = mutableListOf<Favourite>()
            val limit = 25
            var offset = 0

            do {
                val favouriteResponse = tablesDB.listRows(
                    databaseId = BuildConfig.DATABASE_ID,
                    tableId = "favourite",
                    queries = listOf(
                        Query.limit(limit),
                        Query.offset(offset)
                    )
                )

                val favourites = favouriteResponse.rows.map { row ->
                    Favourite(
                        id = row.id,
                        userId = row.data["userId"] as String,
                        carId =row.data["carId"] as String
                    )
                }
                allFavourites.addAll(favourites)
                offset += limit

            }while (favourites.size == limit)

            favouriteDao.insertAll(allFavourites)


        }catch (e: Exception){
            throw e
        }
    }
}