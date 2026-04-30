package com.example.carspotter.auth

import io.appwrite.models.User

sealed interface AuthState {
    data object Loading: AuthState
    data class Authenticated(val user: User<Map<String, Any>>):AuthState
    data class Unauthenticated(
        val errorMessage: String? = null,
        val errorId: Long = 0L,
    ):AuthState


}
