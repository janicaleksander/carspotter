package com.example.carspotter.util

import com.example.carspotter.models.Converters
import com.example.carspotter.models.MediaTypeEnum
import com.example.carspotter.models.SyncState
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ConvertersTest {

    private val converters = Converters()


    @Test
    fun `fromLocalDateTime null returns null`() {
        assertNull(converters.fromLocalDateTime(null))
    }

    @Test
    fun `fromLocalDateTime produces non-null string`() {
        assertNotNull(converters.fromLocalDateTime(LocalDateTime.of(2025, 1, 15, 10, 30)))
    }

    @Test
    fun `toLocalDateTime null returns null`() {
        assertNull(converters.toLocalDateTime(null))
    }

    @Test
    fun `toLocalDateTime parses plain LocalDateTime string`() {
        val original = LocalDateTime.of(2025, 6, 1, 12, 0, 0)
        val parsed = converters.toLocalDateTime(original.toString())
        assertEquals(original, parsed)
    }


    @Test
    fun `fromLocalDateTime and toLocalDateTime are inverse operations`() {
        val original = LocalDateTime.of(2025, 11, 5, 8, 45, 0)
        val serialized = converters.fromLocalDateTime(original)
        val restored = converters.toLocalDateTime(serialized)
        assertEquals(original, restored)
    }


    @Test
    fun `fromMediaTypeEnum returns correct string for each value`() {
        assertEquals("photo", converters.fromMediaTypeEnum(MediaTypeEnum.PHOTO))
        assertEquals("video", converters.fromMediaTypeEnum(MediaTypeEnum.VIDEO))
        assertEquals("audio", converters.fromMediaTypeEnum(MediaTypeEnum.AUDIO))
    }

    @Test
    fun `toMediaTypeEnum round-trips all values`() {
        MediaTypeEnum.entries.forEach { type ->
            assertEquals(type, converters.toMediaTypeEnum(converters.fromMediaTypeEnum(type)))
        }
    }

    @Test
    fun `fromSyncState returns name of each enum value`() {
        assertEquals("SYNCED",         converters.fromSyncState(SyncState.SYNCED))
        assertEquals("PENDING_UPDATE",  converters.fromSyncState(SyncState.PENDING_UPDATE))
        assertEquals("PENDING_DELETE",  converters.fromSyncState(SyncState.PENDING_DELETE))
        assertEquals("PENDING_CREATE",  converters.fromSyncState(SyncState.PENDING_CREATE))
    }

    @Test
    fun `toSyncState round-trips all values`() {
        SyncState.entries.forEach { state ->
            assertEquals(state, converters.toSyncState(converters.fromSyncState(state)))
        }
    }
}