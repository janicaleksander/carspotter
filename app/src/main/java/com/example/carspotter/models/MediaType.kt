package com.example.carspotter.models

enum class MediaTypeEnum(val value: String) {
    PHOTO("photo"),
    VIDEO("video"),
    AUDIO("audio");

    companion object {
        fun fromValue(value: String): MediaTypeEnum =
            MediaTypeEnum.entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown CollectionType: $value")
    }
}