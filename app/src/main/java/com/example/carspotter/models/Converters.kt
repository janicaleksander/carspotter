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
        @TypeConverter
        fun fromCollectionTypeEnum(collectionType: CollectionTypeEnum): String {
            return collectionType.value
        }

        @TypeConverter
        fun toCollectionTypeEnum(value: String): CollectionTypeEnum {
            return CollectionTypeEnum.values().first { it.value == value }
        }


}