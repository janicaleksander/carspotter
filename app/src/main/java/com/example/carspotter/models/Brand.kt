package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brand")
data class Brand (
    @PrimaryKey
    val name: String
)