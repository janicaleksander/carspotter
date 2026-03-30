package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "brand")
data class Brand (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String
)