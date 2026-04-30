package com.example.carspotter.auth

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.Preferences
import io.appwrite.models.User
import io.appwrite.services.Account
import io.appwrite.services.TablesDB
import java.time.Instant
import javax.inject.Inject

class AccountService @Inject constructor(
    private val account: Account,
    private val tablesDB: TablesDB,
    @ApplicationContext context: Context
) {
    private val sessionPrefs =
        context.getSharedPreferences(SESSION_CACHE_PREFS, Context.MODE_PRIVATE)

    suspend fun getLoggedIn(): User<Map<String, Any>>? {
        return try {
            val user = account.get()
            cacheUserId(user.id)
            cacheCurrentSessionId(user.id)
            user
        } catch (e: AppwriteException) {
            if (e.code == HTTP_UNAUTHORIZED) {
                clearCachedSession()
                null
            } else {
                getCachedOfflineUserOrNull().also {
                    if (it != null) {
                        Log.w(
                            TAG,
                            "Using cached auth stub after Appwrite error ${e.code}: ${e.message}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            getCachedOfflineUserOrNull().also {
                if (it != null) {
                    Log.w(TAG, "Using cached auth stub after network/auth failure", e)
                }
            }
        }
    }


    suspend fun login(email: String, password: String): User<Map<String, Any>>? {
        return try {
            try {
                account.deleteSession("current")
            } catch (_: AppwriteException) {
                Log.w(TAG, "No existing session to delete before login")
            }

            val session = account.createEmailPasswordSession(
                email = email,
                password = password
            )

            cacheSession(session.id, session.userId)
            getLoggedIn()
        } catch (e: AppwriteException) {
            throw e
        }
    }

    suspend fun register(
        nickname: String,
        email: String,
        password: String
    ): User<Map<String, Any>>? {
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
            Log.d(TAG, "Registration error: ${e.message}")
            throw e
        }
    }

    suspend fun logout() {
        try {
            account.deleteSessions()
            clearCachedSession()
        } catch (e: AppwriteException) {
            if (e.code == HTTP_UNAUTHORIZED) {
                clearCachedSession()
                return
            }
            throw e
        }
    }

    private suspend fun cacheCurrentSessionId(userId: String) {
        if (!sessionPrefs.getString(KEY_SESSION_ID, null).isNullOrBlank()) {
            cacheUserId(userId)
            return
        }

        try {
            val session = account.getSession("current")
            cacheSession(session.id, userId)
        } catch (_: AppwriteException) {
            cacheUserId(userId)
        }
    }

    private fun cacheUserId(userId: String) {
        sessionPrefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    private fun cacheSession(sessionId: String, userId: String) {
        sessionPrefs.edit()
            .putString(KEY_SESSION_ID, sessionId)
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    private fun clearCachedSession() {
        sessionPrefs.edit().clear().apply()
    }

    private fun getCachedOfflineUserOrNull(): User<Map<String, Any>>? {
        val userId = sessionPrefs.getString(KEY_USER_ID, null)
        val sessionId = sessionPrefs.getString(KEY_SESSION_ID, null)

        if (userId.isNullOrBlank() || sessionId.isNullOrBlank()) {
            return null
        }

        val now = Instant.now().toString()

        return User(
            id = userId,
            createdAt = now,
            updatedAt = now,
            name = "Offline user",
            password = null,
            hash = null,
            hashOptions = null,
            registration = now,
            status = true,
            labels = emptyList(),
            passwordUpdate = now,
            email = "",
            phone = "",
            emailVerification = false,
            phoneVerification = false,
            mfa = false,
            prefs = Preferences(
                mapOf(
                    "offlineStub" to true,
                    "cachedSessionId" to sessionId
                )
            ),
            targets = emptyList(),
            accessedAt = now
        )
    }

    private companion object {
        const val TAG = "AccountService"
        const val SESSION_CACHE_PREFS = "auth_session_cache"
        const val KEY_SESSION_ID = "session_id"
        const val KEY_USER_ID = "user_id"
        const val HTTP_UNAUTHORIZED = 401
    }
}
