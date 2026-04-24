package com.example.carspotter.ui.garage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Category
import com.example.carspotter.ui.components.DropDown
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.viewmodels.GarageCarUiModel
import com.example.carspotter.viewmodels.GarageUiState

//one selected at one time e.g if category is selected then brand and fav is not selected and vice versa
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageContent(
    uiState: GarageUiState,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    isSelectedFavourite: () -> Unit,
    onCarClick : (String) -> Unit,
    onHeartClick : (String) -> Unit
){
    Scaffold(
        topBar = { TabHeader(title = "YOUR GARAGE") },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key="filter_row") {
                FilterRow(
                    categories = uiState.categories,
                    brands = uiState.brands,
                    selectedCategoryId = uiState.selectedCategoryId,
                    selectedBrandId = uiState.selectedBrandId,
                    isSelectedFavorites = uiState.isSelectedFavorites,
                    onCategorySelected = onCategorySelected,
                    onBrandSelected = onBrandSelected,
                    onSelectedFavorites = isSelectedFavourite
                )
            }

            items(items=uiState.userCars,
                key = { it.carId }) { car ->
                GarageCarCard(
                    car = car,
                    onClick = { onCarClick(car.carId) },
                    onHeartClick = { onHeartClick(car.carId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
            }

        }


    }
}
/*
W app/src/main/java/com/example/carspotter/viewmodels/GarageViewModel.kt uiState jest budowany przez combine(...), ale nie zawiera flow:
_selectedCategoryId
_selectedBrandId
_isSelectedFavorites
* */
@Composable
fun FilterRow(
    categories: List<Category>,
    brands: List<Brand>,
    selectedCategoryId: String?,
    selectedBrandId: String?,
    isSelectedFavorites: Boolean,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onSelectedFavorites: () -> Unit
){
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key="filters") {
            DropDown(
                label = "Category",
                isSelected = selectedCategoryId != null,
                options = categories.map { it.id to it.name },
                onOptionSelected = { onCategorySelected(it) },
            )

            DropDown(
                label = "Brand",
                isSelected = selectedBrandId != null,
                options = brands.map { it.id to it.name },
                onOptionSelected = { onBrandSelected(it) },
            )

            //todo sort by
            //with default label all and then is all or favourite
                DropDown(
                    label = "All",
                    isSelected = isSelectedFavorites,
                    options = listOf("All" to "all", "Favourites" to "favourites"),
                    onOptionSelected = { onSelectedFavorites() },
                )

        }
    }
}



@Composable
fun GarageCarCard(
    car: GarageCarUiModel,
    onClick: () -> Unit,
    onHeartClick: () -> Unit,
    modifier: Modifier = Modifier
){
    Card(
        modifier=modifier
                .fillMaxSize()
                .clickable(onClick=onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column{
                Text(text = car.carId)
                Text(text = car.category, style = MaterialTheme.typography.headlineSmall)
                Text(text = car.brandName, style = MaterialTheme.typography.bodyMedium)
                Text(text = car.imageUrl?: "brak", style = MaterialTheme.typography.bodyMedium)
                //todo image
        }
    }
}


@Composable
fun GarageCarInfoOverlay(
    car: GarageCarUiModel,
    onHeartClick: () -> Unit,
){
    //i need this brand + model as title
    // below to the left year and to the right category

    //also have heart in the right top corner
}