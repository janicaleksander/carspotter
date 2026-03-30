package com.example.carspotter.viewmodels
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carspotter.auth.AccountService
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.Client
import io.appwrite.models.User
import io.appwrite.services.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            val user = accountService.login(email, password)
            authState = if (user!=null){
                AuthState.Authenticated(user)
            }else{
                AuthState.Unauthenticated("Invalid email or password")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            val user = accountService.register(email, password)
            authState = if (user!=null){
                AuthState.Authenticated(user)
            }else{
                AuthState.Unauthenticated("Registration failed. Email may already be in use.")
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