package com.example.carspotter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.carspotter.auth.AuthState
import com.example.carspotter.services.SyncWorker
import com.example.carspotter.ui.components.NavHostComponent
import com.example.carspotter.ui.login.AuthScreen
import com.example.carspotter.ui.theme.CarspotterTheme
import com.example.carspotter.viewmodels.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CarspotterTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                MainAppGate(authViewModel, this)
            }
        }
    }
}

@Composable
fun MainAppGate(authViewModel: AuthViewModel, context: Context) {
    val navController = rememberNavController()

    LaunchedEffect(authViewModel.authState) {
        if (authViewModel.authState is AuthState.Unauthenticated &&
            navController.currentDestination?.route != "gate_screen"
        ) {
            navController.navigate("gate_screen") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = "gate_screen") {
        composable("gate_screen") {
            when (val currentState = authViewModel.authState) {
                is AuthState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is AuthState.Authenticated -> {
                    val user = currentState.user
                    LaunchedEffect(user.id) {
                        val workManager = WorkManager.getInstance(context)

                        val immediateSync = OneTimeWorkRequestBuilder<SyncWorker>().build()
                        workManager.enqueueUniqueWork(
                            "sync_work_immediate",
                            ExistingWorkPolicy.REPLACE,
                            immediateSync,
                        )

                        val periodicSync = PeriodicWorkRequestBuilder<SyncWorker>(
                            15,
                            TimeUnit.MINUTES,
                        ).build()
                        workManager.enqueueUniquePeriodicWork(
                            "sync_work_periodic",
                            ExistingPeriodicWorkPolicy.KEEP,
                            periodicSync,
                        )

                        Log.d(
                            "MainAppGate",
                            "User authenticated: immediate sync started & periodic scheduled",
                        )

                        navController.navigate("main_screen") {
                            popUpTo("gate_screen") { inclusive = true }
                        }
                    }
                }

                is AuthState.Unauthenticated -> {
                    AuthScreen(viewModel = authViewModel)
                }
            }
        }

        composable("main_screen") {
            NavHostComponent(authViewModel)
        }
    }
}
