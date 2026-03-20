package com.example.carspotter.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId:Long, //FK
    val carId:Long, //FK
    val collectionType: CollectionTypeEnum, // enum
    val notes:String,
    @Embedded
    val location: Location,
    val addedAt: LocalDateTime,
)