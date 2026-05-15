package com.example.carspotter.viewmodels

import org.junit.Test
import kotlin.test.Test
import kotlin.test.assertEquals

class SanitizePriceTest {

    @Test
    fun `plain integer passes through unchanged`() {
        assertEquals("12000", sanitizePrice("12000"))
    }

    @Test
    fun `comma is replaced with dot`() {
        assertEquals("1.5", sanitizePrice("1,5"))
    }

    @Test
    fun `letters and symbols are stripped`() {
        assertEquals("1500", sanitizePrice("1 500 PLN"))
    }

    @Test
    fun `only first dot is kept`() {
        assertEquals("1.23", sanitizePrice("1.2.3"))
    }

    @Test
    fun `comma then multiple dots keeps only first separator`() {
        assertEquals("9.99", sanitizePrice("9,9.9"))
    }

    @Test
    fun `empty string returns empty`() {
        assertEquals("", sanitizePrice(""))
    }

    @Test
    fun `negative sign is stripped`() {
        assertEquals("100", sanitizePrice("-100"))
    }
}
