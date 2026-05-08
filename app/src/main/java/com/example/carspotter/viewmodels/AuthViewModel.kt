package com.example.carspotter.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import com.example.carspotter.auth.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val nickname: String = "",
    val passwordVisible: Boolean = false,
    val isLoginMode: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val errorId: Long = 0L,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    var authState by mutableStateOf<AuthState>(AuthState.Loading)
        private set
    private var nextAuthErrorId = 1L
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()


    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val user = withContext(Dispatchers.IO) {
                accountService.getLoggedIn()
            }
            authState = if (user != null) {
                AuthState.Authenticated(user)
            } else {
                _uiState.update { it.copy(isLoading = false) }
                AuthState.Unauthenticated()
            }
        }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun toggleAuthMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                password = "",
                errorMessage = null,
                errorId = 0L,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isLoginMode) {
            login(state.email, state.password)
        } else {
            register(state.nickname, state.email, state.password)
        }
    }

    fun login(email: String, password: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                errorId = 0L,
            )
        }
        viewModelScope.launch {
            try {
                val user = accountService.login(email, password)
                authState = if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            password = "",
                            errorMessage = null,
                            errorId = 0L,
                        )
                    }
                    AuthState.Authenticated(user)
                } else {
                    unauthenticated("Invalid email or password")
                }
            } catch (e: AppwriteException) {
                authState = unauthenticated("Login failed: ${e.message}")
                Log.e("AuthViewModel", "Login error: ${e.message}")
            } catch (e: Exception) {
                authState = unauthenticated("Login failed: ${e.message ?: "Unknown error"}")
                Log.e("AuthViewModel", "Login error: ${e.message}")
            }
        }
    }

    fun register(nickname: String, email: String, password: String) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                errorId = 0L,
            )
        }
        viewModelScope.launch {
            try {
                val user = accountService.register(nickname, email, password)
                authState = if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            password = "",
                            errorMessage = null,
                            errorId = 0L,
                        )
                    }
                    AuthState.Authenticated(user)
                } else {
                    unauthenticated("Registration failed. Email may already be in use.")
                }
            } catch (e: AppwriteException) {
                authState = unauthenticated("Registration failed: ${e.message}")
                Log.e("AuthViewModel", "Register error: ${e.message}")
            } catch (e: Exception) {
                authState = unauthenticated("Registration failed: ${e.message ?: "Unknown error"}")
                Log.e("AuthViewModel", "Register error: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                accountService.logout()
                _uiState.value = AuthUiState()
                authState = AuthState.Unauthenticated()
            } catch (e: AppwriteException) {
                Log.e("AuthViewModel", "Logout error: ${e.message}")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Logout error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun unauthenticated(message: String?): AuthState.Unauthenticated {
        val errorId = if (message == null) 0L else nextAuthErrorId++
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = message,
                errorId = errorId,
            )
        }
        return AuthState.Unauthenticated(errorMessage = message, errorId = errorId)
    }
}
