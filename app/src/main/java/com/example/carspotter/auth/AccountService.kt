package com.example.carspotter.auth

import android.util.Log
import io.appwrite.BuildConfig
import io.appwrite.ID
import io.appwrite.models.User
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import io.appwrite.services.Databases // Upewnij się, że masz ten import
import io.appwrite.services.TablesDB
import javax.inject.Inject

class AccountService @Inject constructor(
    private val account: Account,
    private val tablesDB: TablesDB
) {

    suspend fun getLoggedIn(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: AppwriteException) {
            return null
        }
    }

    suspend fun login(email: String, password: String): User<Map<String, Any>>? {
        return try {
            account.createEmailPasswordSession(
                email = email,
                password = password
            )
            getLoggedIn()
        } catch (e: AppwriteException) {
            throw e;
        }
    }

    suspend fun register(nickname:String,email: String, password: String): User<Map<String, Any>>? {
        val sharedUserID = ID.unique()

        return try {
            account.create(
                userId = sharedUserID,
                email = email,
                password = password
            )

            tablesDB.createRow(
                databaseId = com.example.carspotter.BuildConfig.DATABASE_ID,
                tableId = "user",
                rowId = sharedUserID,
                data = mapOf(
                    "nickname" to nickname
                )
            )

            login(email, password)

        } catch (e: AppwriteException) {
            Log.d("AccountService", "Registration error: ${e.message}")
            throw e;
        }
    }

    suspend fun logout() {
        try {
            account.deleteSession("current")
        } catch (e: AppwriteException) {
           throw e;
        }
    }
}