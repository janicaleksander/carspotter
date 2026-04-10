package com.example.carspotter.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "user_dream",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Car::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )

    ],
    indices = [Index("userId"), Index("carId")]
)
data class UserDream (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId:String, //FK
    val carId: String, //FK
    val syncState: SyncState = SyncState.SYNCED,
    val updatedAt: LocalDateTime

)