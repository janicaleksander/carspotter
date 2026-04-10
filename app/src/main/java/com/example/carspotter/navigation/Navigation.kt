package com.example.carspotter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route:String, val label: String, val icon: ImageVector){
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Garage : Screen("garage", "Garage", Icons.Default.DirectionsCar)
    object New : Screen("new","New", Icons.Default.Add)
    object Tops : Screen("tops","Tops", Icons.Default.Star)
    object Settings : Screen("settings","Settings", Icons.Default.Settings)
}