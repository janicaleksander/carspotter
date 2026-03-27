package com.example.carspotter.repository

import com.example.carspotter.BuildConfig
import com.example.carspotter.dao.UserDao
import com.example.carspotter.models.User
import io.appwrite.services.TablesDB
import java.time.LocalDateTime
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val tablesDB: TablesDB
) {
    //get users itd


    suspend fun syncUser(userId: String){
        try {
            val userResponse = tablesDB.getRow(
                databaseId = BuildConfig.DATABASE_ID,
                tableId = "user",
                rowId = userId
            )

            val user = User(
                id = userId,
                nickname = userResponse.data["nickname"] as String,
                createdAt = LocalDateTime.parse(userResponse.data["createdAt"] as String)
            )
            userDao.insert(user)
        }catch (e: Exception){
            throw e
        }

    }
}