package com.example.carspotter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val CardImageShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

@Composable
fun CarCoverImage(
    imageUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 210.dp,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(CardImageShape),
    ) {
        if (imageUrl == null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray.copy(alpha = 0.8f),
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
                modifier = Modifier.matchParentSize(),
            )
        }

        overlay()
    }
}
