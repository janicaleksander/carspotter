package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.dao.UserDao
import com.example.carspotter.models.Favourite
import com.example.carspotter.models.User
import io.appwrite.Query
import io.appwrite.services.TablesDB
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val favouriteDao: FavouriteDao,
    private val tablesDB: TablesDB
){
    suspend fun getUser(userId:String): User?{
        return userDao.getUser(userId);
    }
    private fun resolveId(value: Any?): String {
        return when (value) {
            is Map<*, *> -> value["\$id"] as String
            is String -> value
            else -> throw IllegalArgumentException("Cannot resolve id from $value")
        }
    }
    suspend fun syncUser(userId:String){
        try{
            val userResponse = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user",
                queries = listOf(
                    Query.equal("\$id",userId),
                    Query.limit(1)
                )
            )
            val user = userResponse.rows.map { row ->
                User(
                    row.id,
                    row.data["nickname"] as String,
                    LocalDateTime.ofInstant(
                        OffsetDateTime.parse(row.data["\$createdAt"] as String).toInstant(),
                        ZoneId.systemDefault()
                    ))
            }
            if (user.isNotEmpty()) {
                userDao.insert(user.first())
            }
        }catch(e: Exception){
            throw e;
        }
        val allFavourites = mutableListOf<Favourite>();
        val limit = 100
        var offset = 0
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
    // user table
    //favourite
}
