package com.example.carspotter.viewmodels
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.exceptions.AppwriteException
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.carspotter.auth.AuthState

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val accountService: AccountService
) : ViewModel() {
    var authState by mutableStateOf<AuthState>(AuthState.Loading)
        private set


    init {
        checkSession()
    }

    fun checkSession(){
        viewModelScope.launch {
            val user = accountService.getLoggedIn()
            authState = if (user!=null){
                AuthState.Authenticated(user)
            }else{
                AuthState.Unauthenticated()
            }
        }
    }


    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = accountService.login(email, password)
                authState = if (user!=null){
                    AuthState.Authenticated(user)
                }else{
                    AuthState.Unauthenticated("Invalid email or password")
                }
            } catch (e: AppwriteException) {
                authState = AuthState.Unauthenticated("Login failed: ${e.message}")
                Log.e("AuthViewModel", "Login error: ${e.message}")
            } catch (e: Exception) {
                authState = AuthState.Unauthenticated("Login failed: ${e.message ?: "Unknown error"}")
                Log.e("AuthViewModel", "Login error: ${e.message}")
            }
        }
    }

    fun register(nickname: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = accountService.register(nickname,email, password)
                authState = if (user!=null){
                    AuthState.Authenticated(user)
                }else{
                    AuthState.Unauthenticated("Registration failed. Email may already be in use.")
                }
            } catch (e: AppwriteException) {
                authState = AuthState.Unauthenticated("Registration failed: ${e.message}")
                Log.e("AuthViewModel", "Register error: ${e.message}")
            } catch (e: Exception) {
                authState = AuthState.Unauthenticated("Registration failed: ${e.message ?: "Unknown error"}")
                Log.e("AuthViewModel", "Register error: ${e.message}")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            accountService.logout()
            authState = AuthState.Unauthenticated()
        }
    }



}