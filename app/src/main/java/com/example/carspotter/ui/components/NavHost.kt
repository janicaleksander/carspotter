package com.example.carspotter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            //home
            composable(Screen.Home.route) {
                val vm: HomeViewModel = hiltViewModel()
                HomeScreen(viewModel = vm)
            }


            //garage
            composable(Screen.Garage.route) {
                val vm: GarageViewModel = hiltViewModel()
                GarageScreen(
                    viewModel = vm,
                    onCarClick = { carId -> navController.navigate("garage_car_detail/$carId") },
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
                    onCarClick = {carId -> navController.navigate("dream_car_detail/$carId")})
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