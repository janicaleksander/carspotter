package com.example.carspotter.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.BorderStroke
import com.example.carspotter.ui.components.CarCoverImage
import com.example.carspotter.ui.components.CustomizableSearchBar
import com.example.carspotter.ui.components.DropDown
import com.example.carspotter.ui.components.EmptyListHint
import com.example.carspotter.ui.components.AppBranding
import com.example.carspotter.ui.theme.CarRed
import com.example.carspotter.viewmodels.HomeFilterMode
import com.example.carspotter.viewmodels.HomeBrowseMode
import com.example.carspotter.viewmodels.HomeCarUiModel
import com.example.carspotter.viewmodels.HomeUiState

private val DreamOrange = Color(0xFFFF6A00)
private val HomeCardShape = RoundedCornerShape(16.dp)

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onFilterModeSelected: (HomeFilterMode) -> Unit,
    onBrowseModeSelected: (Boolean) -> Unit,
    onUserCarClick: (String) -> Unit,
    onDreamCarClick: (String) -> Unit,
    onHeartClick: (String) -> Unit,
) {
    Scaffold(
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
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {

            item(key = "branding") {
                AppBranding(
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item(key = "search") {
                CustomizableSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = onSearchQueryChange,
                    searchResults = uiState.homeCars
                        .map { it.brandName }
                        .distinct(),
                    onResultClick = onSearchQueryChange,
                    modifier = Modifier.statusBarsPadding(),
                    placeholder = { Text("FIND SPOT") },
                )
            }

            item(key = "filters") {
                HomeFilterRow(
                    uiState = uiState,
                    onCategorySelected = onCategorySelected,
                    onFilterModeSelected = onFilterModeSelected,
                    onBrowseModeSelected = onBrowseModeSelected,
                )
            }

            item(key = "section_title") {
                HomeSectionTitle(
                    title = if (uiState.browseMode == HomeBrowseMode.DREAMS) "YOUR DREAMS" else "RECENT SPOTS",
                )
            }

            if (uiState.homeCars.isEmpty()) {
                item(key = "empty") {
                    HomeEmptyState(uiState = uiState)
                }
            } else {
                items(
                    items = uiState.homeCars,
                    key = { "${it.carId}_${it.isDream}" },
                ) { car ->
                    HomeSpotCard(
                        car = car,
                        onClick = {
                            if (car.isDream) onDreamCarClick(car.carId)
                            else onUserCarClick(car.carId)
                        },
                        onHeartClick = if (car.isDream) null else ({ onHeartClick(car.carId) }),
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFilterRow(
    uiState: HomeUiState,
    onCategorySelected: (String?) -> Unit,
    onFilterModeSelected: (HomeFilterMode) -> Unit,
    onBrowseModeSelected: (Boolean) -> Unit,
) {
    val modeOptions = if (uiState.browseMode == HomeBrowseMode.DREAMS) {
        listOf(HomeFilterMode.ALL.name to "All")
    } else {
        listOf(
            HomeFilterMode.ALL.name to "All",
            HomeFilterMode.FAVOURITES.name to "Favourites",
        )
    }
    val categoryOptions = listOf<String?>(null) + uiState.categories.map { it.id as String? }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item(key = "mode") {
            DropDown(
                label = when (uiState.filterMode) {
                    HomeFilterMode.ALL -> "All"
                    HomeFilterMode.FAVOURITES -> "Favourites"
                },
                isSelected = uiState.filterMode == HomeFilterMode.FAVOURITES,
                options = modeOptions,
                onOptionSelected = { selected ->
                    val mode = HomeFilterMode.valueOf(selected ?: HomeFilterMode.ALL.name)
                    onFilterModeSelected(mode)
                },
            )
        }

        item(key = "category") {
            DropDown(
                label = uiState.categories.firstOrNull { it.id == uiState.selectedCategoryId }?.name ?: "Category",
                isSelected = uiState.selectedCategoryId != null,
                options = categoryOptions.map { id ->
                    id to (uiState.categories.firstOrNull { it.id == id }?.name ?: "All")
                }.let { options -> listOf(null to "All") + options.filter { it.first != null } },
                onOptionSelected = onCategorySelected,
            )
        }

        item(key = "dreams") {
            FilterChip(
                selected = uiState.browseMode == HomeBrowseMode.DREAMS,
                onClick = { onBrowseModeSelected(uiState.browseMode != HomeBrowseMode.DREAMS) },
                label = {
                    Text(
                        text = "DREAMS",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                shape = RoundedCornerShape(50),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color.Transparent,
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface,
                    containerColor = Color.Transparent,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (uiState.browseMode == HomeBrowseMode.DREAMS) CarRed
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                ),
            )
        }
    }
}

@Composable
private fun HomeSectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 6.dp, height = 32.dp)
                .background(Color.Black, RoundedCornerShape(50)),
        )
        Spacer(modifier = Modifier.size(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun HomeEmptyState(uiState: HomeUiState) {
    val (primary, secondary) = when {
        uiState.browseMode == HomeBrowseMode.DREAMS ->
            "No dream cars yet" to "Save dream cars from the Tops tab to see them here."
        uiState.filterMode == HomeFilterMode.FAVOURITES && uiState.totalCarCount == 0 ->
            "No favourite spots yet" to "Add spots to your garage first, then mark them with a heart."
        uiState.filterMode == HomeFilterMode.FAVOURITES ->
            "No favourite spots yet" to "Tap the heart on a recent spot to save it here."
        uiState.totalCarCount == 0 ->
            "No recent spots yet" to "Add a new spot from the New tab."
        else ->
            "No cars match these filters" to "Try All, another category, or clear the search."
    }
    EmptyListHint(primary = primary, secondary = secondary)
}

@Composable
private fun HomeSpotCard(
    car: HomeCarUiModel,
    onClick: () -> Unit,
    onHeartClick: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = HomeCardShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        CarCoverImage(
            imageUrl = car.imageUrl,
            contentDescription = car.brandName,
            height = 250.dp,
            overlay = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.18f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.24f),
                                ),
                            ),
                        ),
                )

                if (onHeartClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(48.dp),
                    ) {
                        IconButton(onClick = onHeartClick) {
                            Icon(
                                imageVector = if (car.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (car.isFavourite) "Remove from favourites" else "Add to favourites",
                                tint = Color.White,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (car.isDream) DreamOrange else CarRed,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, bottom = 18.dp),
                ) {
                    Text(
                        text = car.category.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    )
                }
            },
        )
    }
}
