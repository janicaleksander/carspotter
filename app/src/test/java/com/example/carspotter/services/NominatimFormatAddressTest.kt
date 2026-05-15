package com.example.carspotter.services

import org.json.JSONObject
import org.junit.Test
import kotlin.test.assertEquals

class NominatimFormatAddressTest {

    private fun parse(raw: String): String? =
        NominatimReverseGeocoder.formatAddress(JSONObject(raw))

    @Test
    fun `city and country are joined with comma`() {
        assertEquals(
            "Warsaw, Poland",
            parse("""{"address":{"city":"Warsaw","country":"Poland"}}"""),
        )
    }

    @Test
    fun `city wins over town when both present`() {
        assertEquals(
            "Warsaw, PL",
            parse("""{"address":{"city":"Warsaw","town":"Praga","country":"PL"}}"""),
        )
    }

    @Test
    fun `falls back to display_name when address object is absent`() {
        assertEquals(
            "Some Place, Somewhere",
            parse("""{"display_name":"Some Place, Somewhere"}"""),
        )
    }
}
