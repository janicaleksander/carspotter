package com.example.carspotter.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale


internal object NominatimReverseGeocoder {

    suspend fun resolve(
        latitude: Double,
        longitude: Double,
        userAgent: String,
    ): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(
                "https://nominatim.openstreetmap.org/reverse?format=jsonv2" +
                        "&lat=${"%.6f".format(Locale.US, latitude)}" +
                        "&lon=${"%.6f".format(Locale.US, longitude)}" +
                        "&zoom=14&addressdetails=1&accept-language=en",
            )
            (url.openConnection() as HttpURLConnection).run {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", userAgent)
                try {
                    if (responseCode != 200) return@withContext null
                    val body = inputStream.bufferedReader().use { it.readText() }
                    formatAddress(JSONObject(body))
                } finally {
                    disconnect()
                }
            }
        }.getOrNull()
    }

    private fun formatAddress(json: JSONObject): String? {
        val address = json.optJSONObject("address") ?: return json.optString("display_name")
            .takeIf { it.isNotBlank() }
        val city = listOf("city", "town", "village", "hamlet", "municipality")
            .firstNotNullOfOrNull { address.optString(it).takeIf(String::isNotBlank) }
        val region = listOf("state", "county", "region")
            .firstNotNullOfOrNull { address.optString(it).takeIf(String::isNotBlank) }
        val country = address.optString("country").takeIf(String::isNotBlank)
        return listOfNotNull(city, region, country)
            .distinct()
            .take(2)
            .joinToString(", ")
            .ifBlank { json.optString("display_name").takeIf(String::isNotBlank) }
    }
}