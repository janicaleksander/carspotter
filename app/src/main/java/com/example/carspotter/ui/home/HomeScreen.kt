package com.example.carspotter.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carspotter.viewmodels.HomeViewModel

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.navigation.compose.rememberNavController
import com.example.carspotter.ui.components.BottomNavBar
import com.example.carspotter.ui.components.NavHostComponent

@Composable
fun HomeScreen(viewModel: HomeViewModel){
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { paddingValues ->
        NavHostComponent(navController, paddingValues, viewModel)
    }

}
