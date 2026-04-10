package com.example.carspotter.repository

import android.util.Log
import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.FavouriteDao
import com.example.carspotter.dao.UserDao
import com.example.carspotter.models.Converters
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

    suspend fun syncUser(userId:String){
        val converters = Converters()
        try{
            val userResponse = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user",
                queries = listOf(
                    Query.equal($$"$id",userId),
                    Query.limit(1)
                )
            )
            val user = userResponse.rows.map { row ->
                User(
                    row.id,
                    row.data["nickname"] as String,
                    updatedAt =converters.toLocalDateTime(row.updatedAt as String) ?: LocalDateTime.now()
                    )
            }
            if (user.isNotEmpty()) {
                userDao.insert(user.first())
            }
        }catch(e: Exception){
            throw e;
        }
    }
}
