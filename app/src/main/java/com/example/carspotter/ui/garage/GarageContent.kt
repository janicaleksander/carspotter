package com.example.carspotter.ui.garage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carspotter.models.Brand
import com.example.carspotter.models.Category
import com.example.carspotter.ui.components.DropDown
import com.example.carspotter.ui.components.EmptyListHint
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.GarageCarUiModel
import com.example.carspotter.viewmodels.GarageFilterMode
import com.example.carspotter.viewmodels.GarageUiState
import com.example.carspotter.viewmodels.SortOption

// ─── Root screen composable ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarageContent(
    uiState: GarageUiState,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onFilterModeSelected: (GarageFilterMode) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onCarClick: (String) -> Unit,
    onHeartClick: (String) -> Unit,
) {
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "filter_row") {
                FilterRow(
                    categories = uiState.categories,
                    brands = uiState.brands,
                    selectedCategoryId = uiState.selectedCategoryId,
                    selectedBrandId = uiState.selectedBrandId,
                    filterMode = uiState.filterMode,
                    sortOption = uiState.sortOption,
                    onCategorySelected = onCategorySelected,
                    onBrandSelected = onBrandSelected,
                    onFilterModeSelected = onFilterModeSelected,
                    onSortSelected = onSortSelected,
                )
            }

            if (uiState.userCars.isEmpty()) {
                item(key = "empty_garage") {
                    GarageEmptyHint(uiState = uiState)
                }
            } else {
                items(items = uiState.userCars, key = { it.carId }) { car ->
                    GarageCarCard(
                        car = car,
                        onClick = { onCarClick(car.carId) },
                        onHeartClick = { onHeartClick(car.carId) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun GarageEmptyHint(uiState: GarageUiState) {
    val (primary, secondary) = when {
        uiState.filterMode == GarageFilterMode.FAVOURITES && uiState.totalGarageCarCount == 0 ->
            "No favourite cars yet" to "Add spots to your garage first, then tap the heart."
        uiState.filterMode == GarageFilterMode.FAVOURITES ->
            "No favourite cars yet" to "Tap the heart on a car card to save it here."
        uiState.totalGarageCarCount == 0 ->
            "Your garage is empty" to "Add a new spot from the New tab."
        else ->
            "No cars match these filters" to "Try All, or change category or brand."
    }
    EmptyListHint(primary = primary, secondary = secondary)
}

// ─── Filter row ─────────────────────────────────────────────────────────────────

@Composable
fun FilterRow(
    categories: List<Category>,
    brands: List<Brand>,
    selectedCategoryId: String?,
    selectedBrandId: String?,
    filterMode: GarageFilterMode,
    sortOption: SortOption,
    onCategorySelected: (String?) -> Unit,
    onBrandSelected: (String?) -> Unit,
    onFilterModeSelected: (GarageFilterMode) -> Unit,
    onSortSelected: (SortOption) -> Unit,
) {
    val modeOptions: List<Pair<String?, String>> = listOf(
        GarageFilterMode.ALL.name to "All",
        GarageFilterMode.FAVOURITES.name to "Favourites",
    )
    val categoryOptions: List<Pair<String?, String>> =
        listOf<Pair<String?, String>>(null to "All") +
            categories.map { it.id as String? to it.name }
    val brandOptions: List<Pair<String?, String>> =
        listOf<Pair<String?, String>>(null to "All") +
            brands.map { it.id as String? to it.name }
    val sortOptions: List<Pair<String?, String>> = listOf(
        SortOption.NONE.name to "Default",
        SortOption.PRICE_ASC.name to "Price \u2191",
        SortOption.PRICE_DESC.name to "Price \u2193",
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "mode") {
            DropDown(
                label = if (filterMode == GarageFilterMode.FAVOURITES) "Favourites" else "All",
                isSelected = filterMode == GarageFilterMode.FAVOURITES,
                options = modeOptions,
                onOptionSelected = { id ->
                    val mode = GarageFilterMode.valueOf(id ?: GarageFilterMode.ALL.name)
                    onFilterModeSelected(mode)
                },
            )
        }
        item(key = "category") {
            DropDown(
                label = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Category",
                isSelected = selectedCategoryId != null,
                options = categoryOptions,
                onOptionSelected = onCategorySelected,
            )
        }
        item(key = "brand") {
            DropDown(
                label = brands.firstOrNull { it.id == selectedBrandId }?.name ?: "Brand",
                isSelected = selectedBrandId != null,
                options = brandOptions,
                onOptionSelected = onBrandSelected,
            )
        }
        item(key = "sort") {
            DropDown(
                label = when (sortOption) {
                    SortOption.NONE -> "Sort by"
                    SortOption.PRICE_ASC -> "Price \u2191"
                    SortOption.PRICE_DESC -> "Price \u2193"
                },
                isSelected = sortOption != SortOption.NONE,
                options = sortOptions,
                onOptionSelected = { id ->
                    val sort = SortOption.valueOf(id ?: SortOption.NONE.name)
                    onSortSelected(sort)
                },
            )
        }
    }
}

// ─── Car card ───────────────────────────────────────────────────────────────────

@Composable
fun GarageCarCard(
    car: GarageCarUiModel,
    onClick: () -> Unit,
    onHeartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            GarageCarCoverImage(
                imageUrl = car.imageUrl,
                contentDescription = "${car.brandName} ${car.model}",
                isFavourite = car.isFavourite,
                onHeartClick = onHeartClick,
            )
            GarageCarInfoOverlay(car = car)
        }
    }
}

@Composable
private fun GarageCarCoverImage(
    imageUrl: String?,
    contentDescription: String,
    isFavourite: Boolean,
    onHeartClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
    ) {
        if (imageUrl == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray,
                )
            }
        } else {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(400)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                filterQuality = FilterQuality.Medium,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .size(40.dp),
        ) {
            IconButton(onClick = onHeartClick) {
                Icon(
                    imageVector = if (isFavourite) Icons.Default.Favorite
                    else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavourite) "Remove from favourites"
                    else "Add to favourites",
                    tint = if (isFavourite) CarRed
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun GarageCarInfoOverlay(car: GarageCarUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${car.brandName} ${car.model}".trim(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = car.year.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (car.category.isNotBlank()) {
            CategoryChip(label = car.category.uppercase())
        }
    }
}

@Composable
private fun CategoryChip(label: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}
