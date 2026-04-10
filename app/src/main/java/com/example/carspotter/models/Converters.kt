package com.example.carspotter.models

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.OffsetDateTime
class Converters {
        @TypeConverter
        fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

        @TypeConverter
        fun toLocalDateTime(value: String?): LocalDateTime? {
                if (value == null) return null
                return try {
                        OffsetDateTime.parse(value).toLocalDateTime()
                } catch (e: Exception) {
                        LocalDateTime.parse(value)
                }
        }

        @TypeConverter
        fun fromMediaTypeEnum(mediaType: MediaTypeEnum): String {
            return mediaType.value
        }

        @TypeConverter
        fun toMediaTypeEnum(value: String): MediaTypeEnum {
            return MediaTypeEnum.values().first { it.value == value }
        }

        @TypeConverter
        fun fromSyncState(state: SyncState): String = state.name

        @TypeConverter
        fun toSyncState(value: String): SyncState = SyncState.valueOf(value)

        fun resolveId(value: Any?): String {
                return when (value) {
                        is Map<*, *> -> value["\$id"] as String
                        is String -> value
                        else -> throw IllegalArgumentException("Cannot resolve id from $value")
                }
        }


}