package com.example.carspotter.ui.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.carspotter.services.NominatimReverseGeocoder
import java.util.Locale
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Renders an OpenStreetMap centered on the given coordinates with a marker
 * plus extra context (scale bar, OSM copyright, city/country label).
 *
 * Falls back to [MapPlaceholder] when coordinates are missing so the slot
 * always reserves the same layout space.
 */
@Composable
fun OsmMapView(
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
    zoom: Double = 14.0,
    markerTitle: String? = null,
) {
    if (latitude == null || longitude == null) {
        MapPlaceholder(
            latitude = latitude,
            longitude = longitude,
            modifier = modifier,
        )
        return
    }

    val context = LocalContext.current
    val userAgent = remember(context) { context.packageName }

    DisposableEffect(Unit) {
        Configuration.getInstance().load(
            context.applicationContext,
            context.applicationContext
                .getSharedPreferences("osmdroid", Context.MODE_PRIVATE),
        )
        Configuration.getInstance().userAgentValue = userAgent
        onDispose { }
    }

    var address by remember(latitude, longitude) { mutableStateOf<String?>(null) }
    LaunchedEffect(latitude, longitude) {
        address = NominatimReverseGeocoder.resolve(latitude, longitude, userAgent)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            AndroidView(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        setHorizontalMapRepetitionEnabled(false)
                        setVerticalMapRepetitionEnabled(false)
                        // Hide the default +/- zoom buttons; pinch-to-zoom stays.
                        zoomController.setVisibility(
                            org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER,
                        )

                        val point = GeoPoint(latitude, longitude)
                        controller.setZoom(zoom)
                        controller.setCenter(point)

                        overlays.add(
                            Marker(this).apply {
                                position = point
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = markerTitle
                            },
                        )
                        overlays.add(CopyrightOverlay(ctx))
                    }
                },
                update = { map ->
                    val point = GeoPoint(latitude, longitude)
                    map.controller.setZoom(zoom)
                    map.controller.setCenter(point)

                    val existing = map.overlays.filterIsInstance<Marker>().firstOrNull()
                    if (existing != null) {
                        existing.position = point
                        existing.title = markerTitle
                    } else {
                        map.overlays.add(
                            0,
                            Marker(map).apply {
                                position = point
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = markerTitle
                            },
                        )
                    }
                    map.invalidate()
                },
                onRelease = { map -> map.onDetach() },
            )
        }

        LocationLabel(
            address = address,
            latitude = latitude,
            longitude = longitude,
        )
    }
}

@Composable
private fun LocationLabel(
    address: String?,
    latitude: Double,
    longitude: Double,
) {
    val coords = String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.size(10.dp))
            Column {
                Text(
                    text = address ?: "Resolving location…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = coords,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
