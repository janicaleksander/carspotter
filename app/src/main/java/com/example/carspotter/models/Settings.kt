package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "setting")
data class Settings(
    @PrimaryKey
    val id: String,
    val appName: String,
    val author: String,
    val version: String
)
