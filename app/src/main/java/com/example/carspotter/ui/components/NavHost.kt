package com.example.carspotter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.carspotter.navigation.Screen
import com.example.carspotter.ui.garage.GarageDetailScreen
import com.example.carspotter.ui.garage.GarageScreen
import com.example.carspotter.ui.home.HomeScreen
import com.example.carspotter.ui.new_spot.NewSpotScreen
import com.example.carspotter.ui.settings.SettingsScreen
import com.example.carspotter.ui.tops.TopsDetailScreen
import com.example.carspotter.ui.tops.TopsScreen
import com.example.carspotter.viewmodels.AuthViewModel
import com.example.carspotter.viewmodels.GarageDetailViewModel
import com.example.carspotter.viewmodels.GarageViewModel
import com.example.carspotter.viewmodels.HomeViewModel
import com.example.carspotter.viewmodels.NewSpotViewModel
import com.example.carspotter.viewmodels.SettingsViewModel
import com.example.carspotter.viewmodels.TopsDetailViewModel
import com.example.carspotter.viewmodels.TopsUiState
import com.example.carspotter.viewmodels.TopsViewModel


@Composable
fun NavHostComponent(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in setOf(
        Screen.Home.route,
        Screen.Garage.route,
        Screen.Tops.route,
        Screen.Settings.route,
    )

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(tween(200)) { it },
                exit = slideOutVertically(tween(200)) { it },
            ) {
                BottomNavBar(navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(250)) },
            popExitTransition = { fadeOut(tween(200)) },
        ) {

            //home
            composable(Screen.Home.route) {
                val vm: HomeViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = vm,
                    onUserCarClick = { carId ->
                        navController.navigate("garage_car_detail/$carId") {
                            launchSingleTop = true
                        }
                    },
                    onDreamCarClick = { carId ->
                        navController.navigate("dream_car_detail/$carId") {
                            launchSingleTop = true
                        }
                    },
                )
            }

            //garage
            composable(Screen.Garage.route) {
                val vm: GarageViewModel = hiltViewModel()
                GarageScreen(
                    viewModel = vm,
                    onCarClick = { carId ->
                        navController.navigate("garage_car_detail/$carId") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = "garage_car_detail/{carId}",
                arguments = listOf(
                    navArgument("carId"){type = NavType.StringType}
                )
            ){
                val vm : GarageDetailViewModel = hiltViewModel()
                GarageDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            }



            //new
            composable(Screen.New.route) {
                val vm: NewSpotViewModel = hiltViewModel()
                NewSpotScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            }


            //top cars
            composable(Screen.Tops.route) {
                val vm: TopsViewModel = hiltViewModel()
                TopsScreen(
                    viewModel = vm,
                    onCarClick = { carId ->
                        navController.navigate("dream_car_detail/$carId") {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(
                route = "dream_car_detail/{carId}",
                arguments = listOf(
                    navArgument("carId"){type = NavType.StringType}
                )
            ){
                val vm : TopsDetailViewModel = hiltViewModel()
                TopsDetailScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                )
            }



            //settings
            composable(Screen.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(viewModel = vm, authViewModel = authViewModel)
            }
        }
    }
}
