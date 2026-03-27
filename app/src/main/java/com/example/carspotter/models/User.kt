package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(tableName = "user")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nickname: String,
    val createdAt: LocalDateTime
)
