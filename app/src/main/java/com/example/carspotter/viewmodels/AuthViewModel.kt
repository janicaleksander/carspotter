package com.example.carspotter.viewmodels
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.Client
import io.appwrite.services.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val client: Client,
    private val account: Account
) : ViewModel() {

    init {
        testPing()
    }

    private fun testPing() {
        viewModelScope.launch {
            try {
                // Odpalamy ping w tle (Dispatchers.IO) tak jak w ich kodzie
                val response = withContext(Dispatchers.IO) { client.ping() }
                Log.d("AppwriteTest", "Ping zakończony sukcesem! Odpowiedź serwera: $response")
            } catch (e: Exception) {
                Log.e("AppwriteTest", "Błąd Ping: ${e.message}")
            }
        }
    }
}