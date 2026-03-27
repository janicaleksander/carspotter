package com.example.carspotter.models

enum class CollectionTypeEnum(val value: String) {
    GARAGE("GARAGE"),
    DREAM("DREAM");

    companion object {
        fun fromValue(value: String): CollectionTypeEnum =
            entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown CollectionType: $value")
    }
}
