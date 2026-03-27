package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.UserDao
import com.example.carspotter.models.User
import io.appwrite.Query
import io.appwrite.services.TablesDB
import java.time.LocalDateTime
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val tablesDB: TablesDB
) {
    //get users itd

    //TODO so sync func are going to throw exception if there is not result, when the result are mandatory
    //other function are going to return nullabe? or lists

    suspend fun syncUser(userId: String){
        try {
            val userResponse = tablesDB.listRows(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user",
                queries = listOf(
                    Query.equal("\$id", userId),
                    Query.limit(1)
                )
            )
            if (userResponse.rows.isEmpty())    {
                throw Exception("Cant synchronize - user not found")
            }
            val user = userResponse.rows.map { row ->
                User(
                    id = userId,
                    nickname = row.data["nickname"] as String,
                    createdAt = LocalDateTime.parse(row.data["createdAt"] as String)
                )
            }
            userDao.insert(user.first())
        }catch (e: Exception){
            throw e
        }

    }
}