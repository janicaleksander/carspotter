package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "setting")
data class Settings(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val appName: String,
    val author: String,
    val version: String,
    val buildDate: LocalDateTime
)
