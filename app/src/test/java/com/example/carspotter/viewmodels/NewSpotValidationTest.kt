package com.example.carspotter.viewmodels

import com.example.carspotter.models.MediaTypeEnum
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private fun form(
    brandId: String?                  = "brand-1",
    categoryId: String?               = "cat-1",
    model: String                     = "911 GT3",
    year: String                      = "2023",
    price: String                     = "250000.00",
    notes: String                     = "Spotted at Warsaw car meet.",
    location: Pair<Double, Double>?   = 52.2297 to 21.0122,
    media: List<PickedMedia>          = listOf(PickedMedia("/img/photo.jpg", MediaTypeEnum.PHOTO)),
) = NewSpotForm(
    brandId    = brandId,
    categoryId = categoryId,
    model      = model,
    year       = year,
    price      = price,
    notes      = notes,
    location   = location,
    media      = media,
)

class NewSpotValidationTest {

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    fun `valid form produces no errors`() {
        val errors = validateNewSpotForm(form())
        assertNull(errors.media)
        assertNull(errors.brand)
        assertNull(errors.category)
        assertNull(errors.model)
        assertNull(errors.year)
        assertNull(errors.price)
        assertNull(errors.location)
        assertNull(errors.notes)
    }

    // ── media ─────────────────────────────────────────────────────────────────

    @Test
    fun `missing photo produces media error`() {
        assertNotNull(validateNewSpotForm(form(media = emptyList())).media)
    }

    @Test
    fun `only audio without photo produces media error`() {
        val audioOnly = listOf(PickedMedia("/snd/engine.mp3", MediaTypeEnum.AUDIO))
        assertNotNull(validateNewSpotForm(form(media = audioOnly)).media)
    }

    @Test
    fun `two audio files produces media error`() {
        val twoAudio = listOf(
            PickedMedia("/img/photo.jpg", MediaTypeEnum.PHOTO),
            PickedMedia("/snd/a.mp3",     MediaTypeEnum.AUDIO),
            PickedMedia("/snd/b.mp3",     MediaTypeEnum.AUDIO),
        )
        assertNotNull(validateNewSpotForm(form(media = twoAudio)).media)
    }

    @Test
    fun `photo plus one audio is valid`() {
        val photoAndAudio = listOf(
            PickedMedia("/img/photo.jpg", MediaTypeEnum.PHOTO),
            PickedMedia("/snd/a.mp3",     MediaTypeEnum.AUDIO),
        )
        assertNull(validateNewSpotForm(form(media = photoAndAudio)).media)
    }

    // ── brand / category / location ───────────────────────────────────────────

    @Test
    fun `null brandId produces brand error`() {
        assertNotNull(validateNewSpotForm(form(brandId = null)).brand)
    }

    @Test
    fun `blank brandId produces brand error`() {
        assertNotNull(validateNewSpotForm(form(brandId = "   ")).brand)
    }

    @Test
    fun `null location produces location error`() {
        assertNotNull(validateNewSpotForm(form(location = null)).location)
    }

    // ── model ─────────────────────────────────────────────────────────────────

    @Test
    fun `blank model produces model error`() {
        assertNotNull(validateNewSpotForm(form(model = "   ")).model)
    }

    @Test
    fun `model at exact max length is valid`() {
        assertNull(validateNewSpotForm(form(model = "A".repeat(MAX_MODEL_LENGTH))).model)
    }

    @Test
    fun `model over max length produces model error`() {
        assertNotNull(validateNewSpotForm(form(model = "A".repeat(MAX_MODEL_LENGTH + 1))).model)
    }

    // ── year ──────────────────────────────────────────────────────────────────

    @Test
    fun `blank year produces year error`() {
        assertNotNull(validateNewSpotForm(form(year = "")).year)
    }

    @Test
    fun `year 1899 is invalid`() {
        assertNotNull(validateNewSpotForm(form(year = "1899")).year)
    }

    @Test
    fun `year 1900 is valid`() {
        assertNull(validateNewSpotForm(form(year = "1900")).year)
    }

    @Test
    fun `next year is valid`() {
        val nextYear = (LocalDateTime.now().year + 1).toString()
        assertNull(validateNewSpotForm(form(year = nextYear)).year)
    }

    @Test
    fun `year two years ahead is invalid`() {
        val tooFar = (LocalDateTime.now().year + 2).toString()
        assertNotNull(validateNewSpotForm(form(year = tooFar)).year)
    }

    // ── price ─────────────────────────────────────────────────────────────────

    @Test
    fun `blank price produces price error`() {
        assertNotNull(validateNewSpotForm(form(price = "")).price)
    }

    @Test
    fun `price zero produces price error`() {
        assertNotNull(validateNewSpotForm(form(price = "0")).price)
    }

    @Test
    fun `price dot only produces price error`() {
        assertNotNull(validateNewSpotForm(form(price = ".")).price)
    }

    @Test
    fun `positive price is valid`() {
        assertNull(validateNewSpotForm(form(price = "1.00")).price)
    }

    // ── notes ─────────────────────────────────────────────────────────────────

    @Test
    fun `blank notes produces notes error`() {
        assertNotNull(validateNewSpotForm(form(notes = "   ")).notes)
    }

    @Test
    fun `notes at exact max length is valid`() {
        assertNull(validateNewSpotForm(form(notes = "A".repeat(MAX_NOTES_LENGTH))).notes)
    }

    @Test
    fun `notes over max length produces notes error`() {
        assertNotNull(validateNewSpotForm(form(notes = "A".repeat(MAX_NOTES_LENGTH + 1))).notes)
    }
}
