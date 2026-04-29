package com.example.carspotter.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.carspotter.models.Settings
import com.example.carspotter.ui.components.AppBranding
import com.example.carspotter.ui.theme.CarRed
import java.time.format.DateTimeFormatter

// ─── Root screen composable ───────────────────────────────────────────────────

@Composable
fun SettingsContent(
    settings: Settings?,
    onPermissionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    imageUrl: String,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 28.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        AppBranding(appName = settings?.appName)

        Spacer(modifier = Modifier.height(32.dp))

        SettingsAboutSection()

        Spacer(modifier = Modifier.height(40.dp))

        SettingsInfoRows(settings = settings)

        Spacer(modifier = Modifier.height(36.dp))

        SettingsActions(
            onPermissionsClick = onPermissionsClick,
            onLogoutClick = onLogoutClick,
        )

        Spacer(modifier = Modifier.height(32.dp))

        SettingsCarImage(imageUrl = imageUrl)

        Spacer(modifier = Modifier.height(96.dp))
    }
}

// ─── Extracted composables ────────────────────────────────────────────────────


@Composable
fun SettingsAboutSection() {
    Text(
        text = "About",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "App info and preferences",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun SettingsInfoRows(settings: Settings?) {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    SettingsItemRow(
        label = "Author",
        value = settings?.author ?: "Unknown",
        icon = Icons.Default.Person,
    )

    Spacer(modifier = Modifier.height(16.dp))

    SettingsItemRow(
        label = "Version",
        value = settings?.version ?: "Unknown",
        icon = Icons.Default.DirectionsCar,
    )

    Spacer(modifier = Modifier.height(16.dp))

    SettingsItemRow(
        label = "Build Date",
        value = settings?.updatedAt?.format(formatter) ?: "Unknown",
        icon = Icons.Default.DirectionsCar,
    )
}

@Composable
fun SettingsActions(
    onPermissionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    Button(
        onClick = onPermissionsClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "PERMISSIONS",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = MaterialTheme.typography.titleMedium.letterSpacing * 1.4,
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onLogoutClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CarRed,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "LOGOUT",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = MaterialTheme.typography.titleMedium.letterSpacing * 1.4,
        )
    }
}

@Composable
fun SettingsCarImage(imageUrl: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(500)
                .size(coil.size.Size.ORIGINAL)
                .build(),
            contentDescription = "Car image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            filterQuality = FilterQuality.Medium,
        )
    }
}

// ─── Shared primitive ─────────────────────────────────────────────────────────

@Composable
fun SettingsItemRow(
    label: String,
    value: String,
    icon: ImageVector,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
