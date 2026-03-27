package com.example.carspotter.models

enum class CollectionTypeEnum(val value: String) {
    GARAGE("garage"),
    DREAM("dream");

    companion object {
        fun fromValue(value: String): CollectionTypeEnum =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown CollectionType: $value")
    }
}
