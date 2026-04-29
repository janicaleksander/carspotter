package com.example.carspotter.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("carId"),
        Index(value = ["carId", "type"]),
    ]
)
data class Media(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val carId: String, // FK
    val type: MediaTypeEnum, // enum
    val filePath: String,
    val updatedAt: LocalDateTime

)


