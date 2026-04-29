package com.example.carspotter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.carspotter.ui.theme.CarRed

/**
 * Reusable app branding component (logo + app name).
 * Displays a car icon with the app name below it.
 */
@Composable
fun AppBranding(
    appName: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = CarRed,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = appName?.uppercase() ?: "CARSPOTTER",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = CarRed,
            letterSpacing = MaterialTheme.typography.headlineSmall.letterSpacing * 1.5,
        )
    }
}

