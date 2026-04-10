package com.example.carspotter.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity(
    tableName = "user_car",
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
data class UserCar (
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId:String, //FK
    val carId: String, //FK
    val notes:String,
    @Embedded
    val location: Location,
    val syncState: SyncState = SyncState.SYNCED,

    val updatedAt: LocalDateTime

)