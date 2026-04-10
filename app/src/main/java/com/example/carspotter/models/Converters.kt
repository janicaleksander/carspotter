package com.example.carspotter.models

import androidx.room.TypeConverter
import java.time.LocalDateTime

class Converters {
        @TypeConverter
        fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

        @TypeConverter
        fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

        @TypeConverter
        fun fromMediaTypeEnum(mediaType: MediaTypeEnum): String {
            return mediaType.value
        }

        @TypeConverter
        fun toMediaTypeEnum(value: String): MediaTypeEnum {
            return MediaTypeEnum.values().first { it.value == value }
        }

        private fun resolveId(value: Any?): String {
                return when (value) {
                        is Map<*, *> -> value["\$id"] as String
                        is String -> value
                        else -> throw IllegalArgumentException("Cannot resolve id from $value")
                }
        }


}