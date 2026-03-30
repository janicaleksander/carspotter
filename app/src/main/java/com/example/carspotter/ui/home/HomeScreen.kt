package com.example.carspotter.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

import androidx.compose.foundation.lazy.items  // ← ten import!


@Composable
fun HomeScreen(viewModel: HomeViewModel){
    val currentUserId = viewModel.currentUserId;

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Zalogowano jako: ${currentUserId}", modifier = Modifier.padding(bottom = 16.dp))
    }
    val cars by viewModel.x.collectAsState(initial = emptyList())  // ← by!
    LazyColumn {
        items(cars) { car ->
            Text(text = "Car: ${car.car.model}")
        }
    }
}