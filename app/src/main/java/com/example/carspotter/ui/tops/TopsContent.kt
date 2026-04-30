package com.example.carspotter.ui.tops

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.foundation.layout.WindowInsets
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carspotter.models.Category
import com.example.carspotter.ui.components.CarCoverImage
import com.example.carspotter.ui.components.EmptyListHint
import com.example.carspotter.ui.components.TabHeader
import com.example.carspotter.ui.theme.TopsOrange
import com.example.carspotter.viewmodels.TopCarUiModel
import com.example.carspotter.viewmodels.TopsUiState
import java.util.Locale

private val TopsCardShape = RoundedCornerShape(16.dp)

// ─── Root screen composable ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopsContent(
    uiState: TopsUiState,
    onCategorySelected: (String?) -> Unit,
    onCarClick: (String) -> Unit,
) {
    Scaffold(
        topBar = { TabHeader(title = "DISCOVER TOP CARS") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(key = "filters") {
                CategoryFilterRow(
                    categories = uiState.categories,
                    selectedCategoryId = uiState.selectedCategoryId,
                    onCategorySelected = onCategorySelected,
                )
            }

            if (uiState.topCars.isEmpty()) {
                item(key = "empty_tops") {
                    val primary: String
                    val secondary: String?
                    if (uiState.selectedCategoryId != null) {
                        primary = "No top cars in this category"
                        secondary = "Try another category above."
                    } else {
                        primary = "No top cars to show yet"
                        secondary = null
                    }
                    EmptyListHint(primary = primary, secondary = secondary)
                }
            } else {
                items(
                    items = uiState.topCars,
                    key = { it.carId }
                ) { car ->
                    TopCarCard(
                        car = car,
                        onClick = { onCarClick(car.carId) },
                        modifier = Modifier.padding(horizontal = 20.dp),
                    )
                }
            }
        }
    }
}

// ─── Category filter row (horizontal slider) ────────────────────────────────────

@Composable
fun CategoryFilterRow(
    categories: List<Category>,
    selectedCategoryId: String?,
        onCategorySelected: (String?) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(items = categories, key = { it.id }) { category ->
            CategoryChip(
                label = category.name.uppercase(),
                isSelected = category.id == selectedCategoryId,
                onClick = { onCategorySelected(category.id) },
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge,
            )
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = TopsOrange,
            selectedLabelColor = Color.White,
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) TopsOrange
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        ),
    )
}

// ─── Car card ────────────────────────────────────────────────────────────────────

@Composable
fun TopCarCard(
    car: TopCarUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = TopsCardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column {
            CarCoverImage(
                imageUrl = car.imageUrl,
                contentDescription = "${car.brandName} ${car.model}",
            )
            DreamCarInfoOverlay(car = car)
        }
    }
}



@Composable
fun DreamCarInfoOverlay(car: TopCarUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TopsOrange)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Text(
                text = "${car.brandName} ${car.model}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%,d", car.powerHP)} HP",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "POWER",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatColumn(
                label = "0-100",
                value = "${String.format(Locale.US, "%.1f", car.acceleration)} Seconds",
            )
            StatColumn(
                label = "TOP SPEED",
                value = "${car.maxSpeed.toInt()} MPH",
                alignment = Alignment.End,
            )
        }
    }
}

// ─── Shared primitive ────────────────────────────────────────────────────────────

@Composable
fun StatColumn(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.85f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
