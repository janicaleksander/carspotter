package com.example.carspotter.viewmodels

import org.junit.Test

import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NewSpotErrorsTest {

    @Test
    fun `hasAny is false when form has no errors`() {
        assertFalse(NewSpotErrors().hasAny)
    }

    @Test
    fun `hasAny is true for each individual field error`() {
        val fieldVariants = listOf(
            NewSpotErrors(media    = "Add at least one photo"),
            NewSpotErrors(brand    = "Pick a brand"),
            NewSpotErrors(category = "Pick a category"),
            NewSpotErrors(model    = "Model is required"),
            NewSpotErrors(year     = "Year is required"),
            NewSpotErrors(price    = "Price is required"),
            NewSpotErrors(location = "Pick a location on the map"),
            NewSpotErrors(notes    = "Notes are required"),
        )
        fieldVariants.forEach { errors ->
            assertTrue(errors.hasAny, "expected hasAny=true for $errors")
        }
    }

    @Test
    fun `hasAny is true when all fields have errors simultaneously`() {
        val allErrors = NewSpotErrors(
            media    = "Add at least one photo",
            brand    = "Pick a brand",
            category = "Pick a category",
            model    = "Model is required",
            year     = "Year is required",
            price    = "Price is required",
            location = "Pick a location on the map",
            notes    = "Notes are required",
        )
        assertTrue(allErrors.hasAny)
    }
    @Test
    fun `hasAny is true when one field has error among all other field from one NewSpotErrors()`(){
        val oneError = NewSpotErrors(
            notes = "Notes are required"
        )
        assertTrue(oneError.hasAny)
    }
}
