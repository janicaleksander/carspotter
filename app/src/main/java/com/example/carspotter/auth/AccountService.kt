package com.example.carspotter.auth

import io.appwrite.ID
import io.appwrite.models.User
import io.appwrite.exceptions.AppwriteException
import io.appwrite.services.Account
import javax.inject.Inject

class AccountService @Inject constructor(
    private val account: Account
) {

    suspend fun getLoggedIn(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: AppwriteException) {
            null
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
            null
        }
    }

    suspend fun register(email: String, password: String): User<Map<String, Any>>? {
        val userID = ID.unique()
        return try {
            account.create(
                userId = userID,
                email = email,
                password = password
            )
            login(email, password)
        } catch (e: AppwriteException) {
            null
        }
    }

    suspend fun logout() {
        account.deleteSession("current")
    }
}
