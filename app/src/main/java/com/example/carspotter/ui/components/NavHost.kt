package com.example.carspotter.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.carspotter.navigation.Screen
import com.example.carspotter.ui.home.HomeContent
import com.example.carspotter.viewmodels.HomeViewModel

@Composable
fun NavHostComponent(navController: NavHostController, paddingValues: PaddingValues, viewModel: Any){
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Screen.Home.route) {
            HomeContent(viewModel as HomeViewModel)
        }
        composable(Screen.Garage.route) {
            Text("Garage")
        }
        composable(Screen.New.route) {
            Text("New")
        }
        composable(Screen.Tops.route) {
            Text("Tops")
        }
        composable(Screen.Settings.route) {
            Text("Settings")
        }
    }
}