package com.example.carspotter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carspotter.auth.AuthState
import com.example.carspotter.ui.home.HomeScreen
import com.example.carspotter.ui.login.AuthScreen
import com.example.carspotter.ui.theme.CarspotterTheme
import com.example.carspotter.viewmodels.AuthViewModel
import com.example.carspotter.viewmodels.HomeViewModel
import com.example.carspotter.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import org.jetbrains.annotations.Debug
import java.util.concurrent.TimeUnit
import androidx.compose.runtime.collectAsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()



        setContent {
            CarspotterTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                MainAppGate(authViewModel,this)
            }
        }
    }
}
@Composable
fun MainAppGate(authViewModel: AuthViewModel,context: Context){
    val navController = rememberNavController()
    NavHost(navController=navController, startDestination = "gate_screen"){
        composable("gate_screen") {
            when (val currentState = authViewModel.authState) {
                is AuthState.Loading -> {
                    //TODO
                }

                is AuthState.Authenticated -> {
                    val user = currentState.user
                    LaunchedEffect(user.id) {
                        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
                            15, TimeUnit.MINUTES
                        ).build()

                        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                            "sync_work",
                            ExistingPeriodicWorkPolicy.KEEP,
                            syncWork
                        )
                        Log.d("MainAppGate", "User authenticated, sync worker scheduled")
                        navController.navigate("home_screen/${user.id}")
                    }
                }

                is AuthState.Unauthenticated -> {
                    AuthScreen(viewModel = authViewModel)
                }
            }
        }
        composable("home_screen/{userId}") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = homeViewModel)
        }
    }

}